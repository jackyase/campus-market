package org.example.demo4.trade;

import org.example.demo4.common.BusinessException;
import org.example.demo4.common.CurrentUserService;
import org.example.demo4.common.PageResult;
import org.example.demo4.notification.NotificationService;
import org.example.demo4.product.Product;
import org.example.demo4.product.ProductImage;
import org.example.demo4.product.ProductImageRepository;
import org.example.demo4.product.ProductRepository;
import org.example.demo4.product.ProductStatus;
import org.example.demo4.review.Review;
import org.example.demo4.review.ReviewRepository;
import org.example.demo4.trade.dto.CartItemResponse;
import org.example.demo4.trade.dto.OrderResponse;
import org.example.demo4.user.User;
import org.example.demo4.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TradeService {

    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;
    private final PayService payService;
    private final CurrentUserService currentUserService;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public TradeService(CartItemRepository cartItemRepository,
                        OrderRepository orderRepository,
                        ProductRepository productRepository,
                        ProductImageRepository imageRepository,
                        PayService payService,
                        CurrentUserService currentUserService,
                        ReviewRepository reviewRepository,
                        UserRepository userRepository,
                        NotificationService notificationService) {
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
        this.payService = payService;
        this.currentUserService = currentUserService;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    // ============ 购物车 ============

    public List<CartItemResponse> listCart() {
        Long userId = currentUserService.requireUser().getId();
        List<CartItem> items = cartItemRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (items.isEmpty()) {
            return List.of();
        }
        Map<Long, Product> products = productRepository.findAllById(
                        items.stream().map(CartItem::getProductId).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(Product::getId, Function.identity()));
        Map<Long, String> covers = coverImages(products.keySet());

        return items.stream().map(item -> {
            Product p = products.get(item.getProductId());
            CartItemResponse r = new CartItemResponse();
            r.setId(item.getId());
            r.setProductId(item.getProductId());
            r.setQuantity(item.getQuantity());
            if (p == null) {
                r.setAvailable(false);
                r.setTitle("商品已删除");
            } else {
                r.setTitle(p.getTitle());
                r.setImage(covers.getOrDefault(p.getId(), ""));
                r.setPrice(p.getPrice());
                r.setStock(p.getStock());
                r.setSellerName(p.getSellerName());
                r.setCondition(p.getCondition().name());
                r.setAvailable(p.getStatus() == ProductStatus.ON_SALE);
            }
            return r;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void addToCart(Long productId, Integer quantity) {
        Long userId = currentUserService.requireUser().getId();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        if (product.getStatus() != ProductStatus.ON_SALE) {
            throw new BusinessException("商品已下架或已售出");
        }
        if (product.getSellerId().equals(userId)) {
            throw new BusinessException("不能购买自己发布的商品");
        }
        int qty = quantity == null || quantity < 1 ? 1 : quantity;
        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseGet(CartItem::new);
        if (item.getId() == null) {
            item.setUserId(userId);
            item.setProductId(productId);
            item.setQuantity(qty);
        } else {
            item.setQuantity(item.getQuantity() + qty);
        }
        cartItemRepository.save(item);
    }

    @Transactional
    public void updateQuantity(Long cartItemId, Integer quantity) {
        Long userId = currentUserService.requireUser().getId();
        CartItem item = cartItemRepository.findByIdAndUserId(cartItemId, userId)
                .orElseThrow(() -> new BusinessException("购物车项不存在"));
        if (quantity == null || quantity < 1) {
            throw new BusinessException("数量至少为 1");
        }
        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    @Transactional
    public void removeItem(Long cartItemId) {
        Long userId = currentUserService.requireUser().getId();
        CartItem item = cartItemRepository.findByIdAndUserId(cartItemId, userId)
                .orElseThrow(() -> new BusinessException("购物车项不存在"));
        cartItemRepository.delete(item);
    }

    // ============ 订单 ============

    @Transactional
    public OrderResponse createOrder(Long productId, Integer quantity) {
        User buyer = currentUserService.requireUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        if (product.getStatus() != ProductStatus.ON_SALE) {
            throw new BusinessException("商品已下架或已售出");
        }
        if (product.getSellerId().equals(buyer.getId())) {
            throw new BusinessException("不能购买自己发布的商品");
        }
        int qty = quantity == null || quantity < 1 ? 1 : quantity;
        if (qty > product.getStock()) {
            throw new BusinessException("库存不足");
        }
        Order order = buildOrder(buyer, product, qty);
        orderRepository.save(order);
        notificationService.notify(order.getSellerId(), NotificationService.TYPE_ORDER,
                "有买家下单了「" + order.getProductTitle() + "」", order.getId());
        return toResponse(order);
    }

    @Transactional
    public List<OrderResponse> checkout(List<Long> cartItemIds) {
        User buyer = currentUserService.requireUser();
        List<OrderResponse> result = new ArrayList<>();
        for (Long cartItemId : cartItemIds) {
            CartItem item = cartItemRepository.findByIdAndUserId(cartItemId, buyer.getId())
                    .orElseThrow(() -> new BusinessException("购物车项不存在"));
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new BusinessException("商品不存在"));
            if (product.getStatus() != ProductStatus.ON_SALE) {
                throw new BusinessException("商品「" + product.getTitle() + "」已下架或已售出");
            }
            if (product.getSellerId().equals(buyer.getId())) {
                throw new BusinessException("不能购买自己发布的商品");
            }
            if (item.getQuantity() > product.getStock()) {
                throw new BusinessException("商品「" + product.getTitle() + "」库存不足");
            }
            Order order = buildOrder(buyer, product, item.getQuantity());
            orderRepository.save(order);
            notificationService.notify(order.getSellerId(), NotificationService.TYPE_ORDER,
                    "有买家下单了「" + order.getProductTitle() + "」", order.getId());
            cartItemRepository.delete(item);
            result.add(toResponse(order));
        }
        return result;
    }

    public PageResult<OrderResponse> listOrders(OrderStatus status, int page, int size) {
        Long buyerId = currentUserService.requireUser().getId();
        Page<Order> pageResult = status == null
                ? orderRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId,
                        PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50)))
                : orderRepository.findByBuyerIdAndStatusOrderByCreatedAtDesc(buyerId, status,
                        PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50)));
        List<OrderResponse> list = pageResult.getContent().stream()
                .map(this::toResponse).collect(Collectors.toList());
        fillReviewed(list);
        return new PageResult<>(list, pageResult.getTotalElements(), page, size);
    }

    public Map<String, Long> statusCounts() {
        Long buyerId = currentUserService.requireUser().getId();
        return orderRepository.countGroupByStatus(buyerId).stream()
                .collect(Collectors.toMap(
                        row -> ((OrderStatus) row[0]).name(),
                        row -> (Long) row[1]));
    }

    public List<OrderResponse> listSoldOrders() {
        Long sellerId = currentUserService.requireUser().getId();
        List<Order> orders = orderRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
        List<OrderResponse> list = orders.stream().map(this::toResponse).collect(Collectors.toList());
        fillReviewed(list);
        Set<Long> buyerIds = list.stream().map(OrderResponse::getBuyerId).collect(Collectors.toSet());
        Map<Long, String> buyerNames = userRepository.findAllById(buyerIds).stream()
                .collect(Collectors.toMap(User::getId,
                        u -> u.getNickname() == null || u.getNickname().isBlank()
                                ? u.getUsername() : u.getNickname()));
        list.forEach(r -> r.setBuyerName(buyerNames.getOrDefault(r.getBuyerId(), "买家")));
        return list;
    }

    public OrderResponse orderDetail(Long orderId) {
        Long buyerId = currentUserService.requireUser().getId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));
        if (!order.getBuyerId().equals(buyerId) && !order.getSellerId().equals(buyerId)) {
            throw new BusinessException(403, "无权查看该订单");
        }
        OrderResponse resp = toResponse(order);
        resp.setReviewed(reviewRepository.existsByOrderId(orderId));
        return resp;
    }

    @Transactional
    public OrderResponse pay(Long orderId, String method) {
        Long buyerId = currentUserService.requireUser().getId();
        Order order = orderRepository.findByIdAndBuyerIdForUpdate(orderId, buyerId)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException("订单当前状态不可支付");
        }
        String payMethod = method == null || method.isBlank() ? "MOCK" : method;
        String transactionNo = payService.pay(order, payMethod);
        order.setStatus(OrderStatus.PAID);
        order.setPaymentMethod(payMethod);
        order.setTransactionNo(transactionNo);
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);

        // 扣减库存（悲观锁防止并发超卖），库存不足则整体回滚
        Product product = productRepository.findByIdForUpdate(order.getProductId())
                .orElseThrow(() -> new BusinessException("商品不存在"));
        if (product.getStock() < order.getQuantity()) {
            throw new BusinessException("库存不足，无法支付");
        }
        product.setStock(product.getStock() - order.getQuantity());
        if (product.getStock() == 0) {
            product.setStatus(ProductStatus.SOLD);
        }
        productRepository.save(product);
        return toResponse(order);
    }

    @Transactional
    public OrderResponse cancel(Long orderId) {
        Long buyerId = currentUserService.requireUser().getId();
        Order order = orderRepository.findByIdAndBuyerId(orderId, buyerId)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException("只有待付款订单可以取消");
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        return toResponse(order);
    }

    @Transactional
    public OrderResponse ship(Long orderId) {
        Long sellerId = currentUserService.requireUser().getId();
        Order order = orderRepository.findByIdAndSellerId(orderId, sellerId)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        if (order.getStatus() != OrderStatus.PAID) {
            throw new BusinessException("只有已付款订单可以发货");
        }
        order.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(order);
        notificationService.notify(order.getBuyerId(), NotificationService.TYPE_SHIPPED,
                "你的订单「" + order.getProductTitle() + "」已发货", order.getId());
        return toResponse(order);
    }

    @Transactional
    public OrderResponse confirm(Long orderId) {
        Long buyerId = currentUserService.requireUser().getId();
        Order order = orderRepository.findByIdAndBuyerId(orderId, buyerId)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new BusinessException("只有已发货订单可以确认收货");
        }
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);
        notificationService.notify(order.getSellerId(), NotificationService.TYPE_COMPLETED,
                "买家已确认收货「" + order.getProductTitle() + "」", order.getId());
        return toResponse(order);
    }

    // ============ 内部方法 ============

    private Order buildOrder(User buyer, Product product, int quantity) {
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setBuyerId(buyer.getId());
        order.setSellerId(product.getSellerId());
        order.setSellerName(product.getSellerName());
        order.setProductId(product.getId());
        order.setProductTitle(product.getTitle());
        order.setProductImage(coverImages(List.of(product.getId())).getOrDefault(product.getId(), ""));
        order.setPrice(product.getPrice());
        order.setQuantity(quantity);
        order.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        return order;
    }

    private Map<Long, String> coverImages(java.util.Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        return imageRepository.findByProductIdIn(productIds).stream()
                .collect(Collectors.groupingBy(ProductImage::getProductId,
                        Collectors.collectingAndThen(
                                Collectors.minBy(java.util.Comparator.comparing(ProductImage::getSortOrder)),
                                opt -> opt.map(ProductImage::getUrl).orElse(""))));
    }

    private String generateOrderNo() {
        return "O" + System.currentTimeMillis()
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }

    private void fillReviewed(List<OrderResponse> list) {
        List<Long> orderIds = list.stream().map(OrderResponse::getId).collect(Collectors.toList());
        if (orderIds.isEmpty()) {
            return;
        }
        Set<Long> reviewedIds = reviewRepository.findByOrderIdIn(orderIds).stream()
                .map(Review::getOrderId).collect(Collectors.toSet());
        list.forEach(r -> r.setReviewed(reviewedIds.contains(r.getId())));
    }

    private OrderResponse toResponse(Order o) {
        OrderResponse r = new OrderResponse();
        r.setId(o.getId());
        r.setOrderNo(o.getOrderNo());
        r.setBuyerId(o.getBuyerId());
        r.setSellerId(o.getSellerId());
        r.setSellerName(o.getSellerName());
        r.setProductId(o.getProductId());
        r.setProductTitle(o.getProductTitle());
        r.setProductImage(o.getProductImage());
        r.setPrice(o.getPrice());
        r.setQuantity(o.getQuantity());
        r.setTotalAmount(o.getTotalAmount());
        r.setStatus(o.getStatus());
        r.setPaymentMethod(o.getPaymentMethod());
        r.setTransactionNo(o.getTransactionNo());
        r.setPaidAt(o.getPaidAt());
        r.setCreatedAt(o.getCreatedAt());
        return r;
    }
}
