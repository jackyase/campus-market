package org.example.demo4.bargain;

import org.example.demo4.bargain.dto.BargainRequest;
import org.example.demo4.bargain.dto.BargainResponse;
import org.example.demo4.common.BusinessException;
import org.example.demo4.common.CurrentUserService;
import org.example.demo4.notification.NotificationService;
import org.example.demo4.product.Product;
import org.example.demo4.product.ProductRepository;
import org.example.demo4.product.ProductStatus;
import org.example.demo4.user.User;
import org.example.demo4.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BargainService {

    private final BargainRepository bargainRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    public BargainService(BargainRepository bargainRepository,
                          ProductRepository productRepository,
                          UserRepository userRepository,
                          CurrentUserService currentUserService,
                          NotificationService notificationService) {
        this.bargainRepository = bargainRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
    }

    @Transactional
    public BargainResponse create(Long productId, BigDecimal price) {
        User buyer = currentUserService.requireUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));
        if (product.getStatus() != ProductStatus.ON_SALE) {
            throw new BusinessException("商品已下架或已售出");
        }
        if (product.getSellerId().equals(buyer.getId())) {
            throw new BusinessException("不能对自己的商品砍价");
        }
        if (price.compareTo(product.getPrice()) >= 0) {
            throw new BusinessException("出价需低于商品售价");
        }
        if (bargainRepository.existsByProductIdAndBuyerIdAndStatus(
                productId, buyer.getId(), BargainStatus.PENDING)) {
            throw new BusinessException("你已对该商品发起过砍价，请等待卖家处理");
        }
        Bargain b = new Bargain();
        b.setProductId(product.getId());
        b.setProductTitle(product.getTitle());
        b.setBuyerId(buyer.getId());
        b.setSellerId(product.getSellerId());
        b.setPrice(price);
        bargainRepository.save(b);
        notificationService.notify(product.getSellerId(), "BARGAIN",
                "有买家出价 ¥" + price + " 购买你的「" + product.getTitle() + "」", product.getId());
        return toResponse(b);
    }

    public List<BargainResponse> listMine() {
        Long buyerId = currentUserService.requireUser().getId();
        return bargainRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<BargainResponse> listIncoming() {
        Long sellerId = currentUserService.requireUser().getId();
        List<Bargain> list = bargainRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
        if (list.isEmpty()) {
            return List.of();
        }
        Set<Long> buyerIds = list.stream().map(Bargain::getBuyerId).collect(Collectors.toSet());
        Map<Long, User> users = userRepository.findAllById(buyerIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return list.stream().map(b -> {
            BargainResponse r = toResponse(b);
            User u = users.get(b.getBuyerId());
            r.setBuyerName(u == null ? "买家" + b.getBuyerId() : displayName(u));
            return r;
        }).collect(Collectors.toList());
    }

    @Transactional
    public BargainResponse accept(Long id) {
        Bargain b = pendingOfMine(id);
        b.setStatus(BargainStatus.ACCEPTED);
        bargainRepository.save(b);
        notificationService.notify(b.getBuyerId(), "BARGAIN",
                "卖家接受了你的砍价 ¥" + b.getPrice() + "（" + b.getProductTitle() + "）", b.getProductId());
        return toResponse(b);
    }

    @Transactional
    public BargainResponse reject(Long id) {
        Bargain b = pendingOfMine(id);
        b.setStatus(BargainStatus.REJECTED);
        bargainRepository.save(b);
        notificationService.notify(b.getBuyerId(), "BARGAIN",
                "卖家拒绝了你的砍价 ¥" + b.getPrice() + "（" + b.getProductTitle() + "）", b.getProductId());
        return toResponse(b);
    }

    private Bargain pendingOfMine(Long id) {
        Long sellerId = currentUserService.requireUser().getId();
        Bargain b = bargainRepository.findByIdAndSellerId(id, sellerId)
                .orElseThrow(() -> new BusinessException("砍价记录不存在"));
        if (b.getStatus() != BargainStatus.PENDING) {
            throw new BusinessException("该砍价已处理");
        }
        return b;
    }

    private BargainResponse toResponse(Bargain b) {
        BargainResponse r = new BargainResponse();
        r.setId(b.getId());
        r.setProductId(b.getProductId());
        r.setProductTitle(b.getProductTitle());
        r.setBuyerId(b.getBuyerId());
        r.setSellerId(b.getSellerId());
        r.setPrice(b.getPrice());
        r.setStatus(b.getStatus());
        r.setCreatedAt(b.getCreatedAt());
        return r;
    }

    private String displayName(User u) {
        return u.getNickname() == null || u.getNickname().isBlank() ? u.getUsername() : u.getNickname();
    }
}
