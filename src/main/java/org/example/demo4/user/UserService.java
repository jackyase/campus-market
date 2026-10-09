package org.example.demo4.user;

import org.example.demo4.bargain.BargainRepository;
import org.example.demo4.common.BusinessException;
import org.example.demo4.common.CurrentUserService;
import org.example.demo4.community.FollowRepository;
import org.example.demo4.favorite.FavoriteRepository;
import org.example.demo4.message.MessageRepository;
import org.example.demo4.notification.NotificationRepository;
import org.example.demo4.product.Product;
import org.example.demo4.product.ProductImageRepository;
import org.example.demo4.product.ProductRepository;
import org.example.demo4.product.ProductStatus;
import org.example.demo4.report.ReportRepository;
import org.example.demo4.review.ReviewRepository;
import org.example.demo4.store.StoreRepository;
import org.example.demo4.trade.CartItemRepository;
import org.example.demo4.trade.OrderRepository;
import org.example.demo4.user.dto.ProfileUpdateRequest;
import org.example.demo4.user.dto.PublicProfileResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final String GHOST_NAME = "已注销用户";

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ReviewRepository reviewRepository;
    private final FollowRepository followRepository;
    private final StoreRepository storeRepository;
    private final CartItemRepository cartItemRepository;
    private final FavoriteRepository favoriteRepository;
    private final BargainRepository bargainRepository;
    private final MessageRepository messageRepository;
    private final NotificationRepository notificationRepository;
    private final ReportRepository reportRepository;
    private final OrderRepository orderRepository;
    private final CurrentUserService currentUserService;

    public UserService(UserRepository userRepository,
                       ProductRepository productRepository,
                       ProductImageRepository productImageRepository,
                       ReviewRepository reviewRepository,
                       FollowRepository followRepository,
                       StoreRepository storeRepository,
                       CartItemRepository cartItemRepository,
                       FavoriteRepository favoriteRepository,
                       BargainRepository bargainRepository,
                       MessageRepository messageRepository,
                       NotificationRepository notificationRepository,
                       ReportRepository reportRepository,
                       OrderRepository orderRepository,
                       CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.reviewRepository = reviewRepository;
        this.followRepository = followRepository;
        this.storeRepository = storeRepository;
        this.cartItemRepository = cartItemRepository;
        this.favoriteRepository = favoriteRepository;
        this.bargainRepository = bargainRepository;
        this.messageRepository = messageRepository;
        this.notificationRepository = notificationRepository;
        this.reportRepository = reportRepository;
        this.orderRepository = orderRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public User updateProfile(ProfileUpdateRequest req) {
        User user = currentUserService.requireUser();
        if (req.getNickname() != null) user.setNickname(req.getNickname());
        if (req.getAvatar() != null) user.setAvatar(req.getAvatar());
        if (req.getCover() != null) user.setCover(req.getCover());
        if (req.getBio() != null) user.setBio(req.getBio());
        if (req.getGender() != null) user.setGender(req.getGender());
        if (req.getBirthday() != null) user.setBirthday(req.getBirthday());
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getSchool() != null) user.setSchool(req.getSchool());
        if (req.getCollege() != null) user.setCollege(req.getCollege());
        if (req.getStudentId() != null) user.setStudentId(req.getStudentId());
        if (req.getInterests() != null) user.setInterests(req.getInterests());
        return userRepository.save(user);
    }

    public PublicProfileResponse publicProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        PublicProfileResponse r = new PublicProfileResponse();
        r.setId(user.getId());
        r.setUsername(user.getUsername());
        r.setNickname(user.getNickname());
        r.setAvatar(user.getAvatar());
        r.setCover(user.getCover());
        r.setBio(user.getBio());
        r.setGender(user.getGender());
        r.setSchool(user.getSchool());
        r.setCollege(user.getCollege());
        r.setInterests(user.getInterests());
        r.setSellerRating(reviewRepository.avgSellerRating(userId));
        r.setSellerRatingCount(reviewRepository.countBySellerId(userId));
        r.setOnSaleCount(productRepository.countBySellerIdAndStatus(userId, ProductStatus.ON_SALE));
        r.setFollowerCount(followRepository.countByFollowingId(userId));
        r.setFollowingCount(followRepository.countByFollowerId(userId));
        Long viewerId = currentUserService.optionalUserId();
        r.setFollowing(viewerId != null && !viewerId.equals(userId)
                && followRepository.findByFollowerIdAndFollowingId(viewerId, userId).isPresent());
        storeRepository.findByOwnerId(userId).ifPresent(s -> {
            r.setStoreName(s.getName());
            r.setStoreAnnouncement(s.getAnnouncement());
        });
        r.setCreatedAt(user.getCreatedAt());
        return r;
    }

    /**
     * 注销账号（软删除 + 匿名化，贴近真实平台）：
     * - 删除纯个人数据与经营身份：商品及其图片/收藏、店铺、购物车、收藏、砍价、私信、通知、举报、关注
     * - 保留与他人相关的历史记录：帖子、订单、评价，但对外匿名化为「已注销用户」
     * - 用户记录标记 deleted，阻止再次登录；旧 token 一并失效
     */
    @Transactional
    public void deleteAccount() {
        Long userId = currentUserService.requireUser().getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        // 1. 删除在售商品及其图片、相关收藏（账号注销后商品随之下架删除）
        List<Long> productIds = productRepository.findBySellerIdOrderByCreatedAtDesc(userId).stream()
                .map(Product::getId).collect(Collectors.toList());
        if (!productIds.isEmpty()) {
            productImageRepository.deleteByProductIdIn(productIds);
            favoriteRepository.deleteByProductIdIn(productIds);
        }
        productRepository.deleteBySellerId(userId);

        // 2. 删除店铺（经营身份终止）
        storeRepository.deleteByOwnerId(userId);

        // 3. 删除纯个人数据
        cartItemRepository.deleteByUserId(userId);
        favoriteRepository.deleteByUserId(userId);
        bargainRepository.deleteByBuyerId(userId);
        bargainRepository.deleteBySellerId(userId);
        messageRepository.deleteBySenderIdOrReceiverId(userId, userId);
        notificationRepository.deleteByUserId(userId);
        reportRepository.deleteByReporterId(userId);
        followRepository.deleteByFollowerIdOrFollowingId(userId, userId);

        // 4. 匿名化保留订单中的卖家名（订单保留，避免对方历史记录丢失）
        orderRepository.updateSellerNameBySellerId(GHOST_NAME, userId);

        // 5. 标记注销 + 匿名化个人信息（帖子/评价通过 nickname 自动显示「已注销用户」）
        user.setDeleted(true);
        user.setNickname(GHOST_NAME);
        user.setAvatar(null);
        user.setCover(null);
        user.setBio(null);
        user.setPhone(null);
        user.setStudentId(null);
        user.setInterests(null);
        userRepository.save(user);
    }
}
