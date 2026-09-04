package org.example.demo4.user;

import org.example.demo4.common.BusinessException;
import org.example.demo4.common.CurrentUserService;
import org.example.demo4.community.FollowRepository;
import org.example.demo4.product.ProductRepository;
import org.example.demo4.product.ProductStatus;
import org.example.demo4.review.ReviewRepository;
import org.example.demo4.store.StoreRepository;
import org.example.demo4.user.dto.ProfileUpdateRequest;
import org.example.demo4.user.dto.PublicProfileResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final FollowRepository followRepository;
    private final StoreRepository storeRepository;
    private final CurrentUserService currentUserService;

    public UserService(UserRepository userRepository,
                       ProductRepository productRepository,
                       ReviewRepository reviewRepository,
                       FollowRepository followRepository,
                       StoreRepository storeRepository,
                       CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
        this.followRepository = followRepository;
        this.storeRepository = storeRepository;
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
}
