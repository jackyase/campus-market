package org.example.demo4.trade;

import org.example.demo4.common.BusinessException;
import org.example.demo4.common.CurrentUserService;
import org.example.demo4.notification.NotificationService;
import org.example.demo4.product.Product;
import org.example.demo4.product.ProductImageRepository;
import org.example.demo4.product.ProductRepository;
import org.example.demo4.product.ProductStatus;
import org.example.demo4.review.ReviewRepository;
import org.example.demo4.user.User;
import org.example.demo4.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductImageRepository imageRepository;
    @Mock
    private PayService payService;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TradeService tradeService;

    private User buyer(long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    @Test
    void createOrder_buyOwnProduct_throws() {
        Product product = new Product();
        product.setId(100L);
        product.setSellerId(1L);
        product.setStatus(ProductStatus.ON_SALE);

        when(currentUserService.requireUser()).thenReturn(buyer(1L));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> tradeService.createOrder(100L, 1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能购买自己发布的商品");
    }

    @Test
    void pay_orderNotPending_throws() {
        Order order = new Order();
        order.setId(10L);
        order.setBuyerId(1L);
        order.setStatus(OrderStatus.PAID);

        when(currentUserService.requireUser()).thenReturn(buyer(1L));
        when(orderRepository.findByIdAndBuyerIdForUpdate(10L, 1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> tradeService.pay(10L, "MOCK"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("订单当前状态不可支付");
    }

    @Test
    void pay_insufficientStock_throws() {
        Order order = new Order();
        order.setId(10L);
        order.setBuyerId(1L);
        order.setProductId(100L);
        order.setQuantity(2);
        order.setStatus(OrderStatus.PENDING_PAYMENT);

        Product product = new Product();
        product.setId(100L);
        product.setStock(1);

        when(currentUserService.requireUser()).thenReturn(buyer(1L));
        when(orderRepository.findByIdAndBuyerIdForUpdate(10L, 1L)).thenReturn(Optional.of(order));
        when(payService.pay(any(Order.class), anyString())).thenReturn("TXN-1");
        when(productRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> tradeService.pay(10L, "MOCK"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("库存不足，无法支付");
    }
}
