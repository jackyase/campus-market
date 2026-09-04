package org.example.demo4.review;

import org.example.demo4.common.BusinessException;
import org.example.demo4.common.CurrentUserService;
import org.example.demo4.notification.NotificationService;
import org.example.demo4.review.dto.ReviewRequest;
import org.example.demo4.trade.Order;
import org.example.demo4.trade.OrderRepository;
import org.example.demo4.trade.OrderStatus;
import org.example.demo4.user.User;
import org.example.demo4.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ReviewService reviewService;

    private ReviewRequest request() {
        ReviewRequest req = new ReviewRequest();
        req.setOrderId(10L);
        req.setProductRating(5);
        req.setSellerRating(5);
        req.setContent("不错");
        return req;
    }

    private User reviewer(long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    @Test
    void create_notBuyer_throws() {
        Order order = new Order();
        order.setId(10L);
        order.setBuyerId(2L);

        when(currentUserService.requireUser()).thenReturn(reviewer(1L));
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> reviewService.create(request()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权评价该订单");
    }

    @Test
    void create_orderNotCompleted_throws() {
        Order order = new Order();
        order.setId(10L);
        order.setBuyerId(1L);
        order.setStatus(OrderStatus.PENDING_PAYMENT);

        when(currentUserService.requireUser()).thenReturn(reviewer(1L));
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> reviewService.create(request()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("订单完成后才能评价");
    }

    @Test
    void create_alreadyReviewed_throws() {
        Order order = new Order();
        order.setId(10L);
        order.setBuyerId(1L);
        order.setStatus(OrderStatus.COMPLETED);

        when(currentUserService.requireUser()).thenReturn(reviewer(1L));
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(reviewRepository.existsByOrderId(10L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.create(request()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("该订单已评价");
    }
}
