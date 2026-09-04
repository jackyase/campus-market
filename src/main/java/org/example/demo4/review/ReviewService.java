package org.example.demo4.review;

import org.example.demo4.common.BusinessException;
import org.example.demo4.common.CurrentUserService;
import org.example.demo4.notification.NotificationService;
import org.example.demo4.review.dto.ReviewRequest;
import org.example.demo4.review.dto.ReviewResponse;
import org.example.demo4.trade.Order;
import org.example.demo4.trade.OrderRepository;
import org.example.demo4.trade.OrderStatus;
import org.example.demo4.user.User;
import org.example.demo4.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    public ReviewService(ReviewRepository reviewRepository,
                         OrderRepository orderRepository,
                         UserRepository userRepository,
                         CurrentUserService currentUserService,
                         NotificationService notificationService) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
    }

    @Transactional
    public ReviewResponse create(ReviewRequest req) {
        User reviewer = currentUserService.requireUser();
        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new BusinessException("订单不存在"));
        if (!order.getBuyerId().equals(reviewer.getId())) {
            throw new BusinessException(403, "无权评价该订单");
        }
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new BusinessException("订单完成后才能评价");
        }
        if (reviewRepository.existsByOrderId(order.getId())) {
            throw new BusinessException("该订单已评价");
        }
        Review review = new Review();
        review.setOrderId(order.getId());
        review.setReviewerId(reviewer.getId());
        review.setSellerId(order.getSellerId());
        review.setProductId(order.getProductId());
        review.setProductTitle(order.getProductTitle());
        review.setProductRating(req.getProductRating());
        review.setSellerRating(req.getSellerRating());
        review.setContent(req.getContent());
        reviewRepository.save(review);
        notificationService.notify(review.getSellerId(), NotificationService.TYPE_REVIEW,
                "收到一条新评价「" + review.getProductTitle() + "」", order.getId());
        return toResponse(review, reviewer);
    }

    public List<ReviewResponse> listByProduct(Long productId) {
        return toResponses(reviewRepository.findByProductIdOrderByCreatedAtDesc(productId));
    }

    public List<ReviewResponse> listBySeller(Long sellerId) {
        return toResponses(reviewRepository.findBySellerIdOrderByCreatedAtDesc(sellerId));
    }

    private List<ReviewResponse> toResponses(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return List.of();
        }
        Set<Long> reviewerIds = reviews.stream().map(Review::getReviewerId).collect(Collectors.toSet());
        Map<Long, User> users = userRepository.findAllById(reviewerIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return reviews.stream()
                .map(r -> toResponse(r, users.get(r.getReviewerId())))
                .collect(Collectors.toList());
    }

    private ReviewResponse toResponse(Review r, User reviewer) {
        ReviewResponse res = new ReviewResponse();
        res.setId(r.getId());
        res.setReviewerId(r.getReviewerId());
        res.setReviewerName(reviewer == null ? "匿名用户"
                : (reviewer.getNickname() == null || reviewer.getNickname().isBlank()
                        ? reviewer.getUsername() : reviewer.getNickname()));
        res.setReviewerAvatar(reviewer == null ? null : reviewer.getAvatar());
        res.setProductId(r.getProductId());
        res.setProductTitle(r.getProductTitle());
        res.setProductRating(r.getProductRating());
        res.setSellerRating(r.getSellerRating());
        res.setContent(r.getContent());
        res.setCreatedAt(r.getCreatedAt());
        return res;
    }
}
