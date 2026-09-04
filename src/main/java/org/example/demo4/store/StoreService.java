package org.example.demo4.store;

import org.example.demo4.common.BusinessException;
import org.example.demo4.common.CurrentUserService;
import org.example.demo4.product.Product;
import org.example.demo4.product.ProductRepository;
import org.example.demo4.product.ProductStatus;
import org.example.demo4.review.ReviewRepository;
import org.example.demo4.store.dto.StoreRequest;
import org.example.demo4.store.dto.StoreResponse;
import org.example.demo4.trade.OrderRepository;
import org.example.demo4.trade.OrderStatus;
import org.example.demo4.user.User;
import org.example.demo4.user.UserRepository;
import org.example.demo4.user.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final CurrentUserService currentUserService;

    public StoreService(StoreRepository storeRepository,
                        UserRepository userRepository,
                        ProductRepository productRepository,
                        OrderRepository orderRepository,
                        ReviewRepository reviewRepository,
                        CurrentUserService currentUserService) {
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public StoreResponse open(StoreRequest req) {
        User user = currentUserService.requireUser();
        if (user.getStoreId() != null) {
            throw new BusinessException("你已开通店铺");
        }
        Store store = new Store();
        store.setOwnerId(user.getId());
        store.setName(req.getName());
        store.setAnnouncement(req.getAnnouncement());
        storeRepository.save(store);

        user.setRole(UserRole.MERCHANT);
        user.setStoreId(store.getId());
        userRepository.save(user);
        return toResponse(store);
    }

    public StoreResponse getMyStore() {
        User user = currentUserService.requireUser();
        Store store = storeRepository.findByOwnerId(user.getId())
                .orElseThrow(() -> new BusinessException(404, "尚未开店"));
        return toResponse(store);
    }

    @Transactional
    public StoreResponse updateStore(StoreRequest req) {
        User user = currentUserService.requireUser();
        Store store = storeRepository.findByOwnerId(user.getId())
                .orElseThrow(() -> new BusinessException(404, "尚未开店"));
        store.setName(req.getName());
        store.setAnnouncement(req.getAnnouncement());
        storeRepository.save(store);
        return toResponse(store);
    }

    public StoreResponse getStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(404, "店铺不存在"));
        return toResponse(store);
    }

    @Transactional
    public void close() {
        User user = currentUserService.requireUser();
        Store store = storeRepository.findByOwnerId(user.getId())
                .orElseThrow(() -> new BusinessException(404, "尚未开店"));
        storeRepository.delete(store);
        user.setRole(UserRole.USER);
        user.setStoreId(null);
        userRepository.save(user);

        List<Product> onSale = productRepository
                .findBySellerIdAndStatusOrderByCreatedAtDesc(user.getId(), ProductStatus.ON_SALE);
        onSale.forEach(p -> p.setStatus(ProductStatus.OFF_SALE));
        productRepository.saveAll(onSale);
    }

    private StoreResponse toResponse(Store s) {
        StoreResponse r = new StoreResponse();
        r.setId(s.getId());
        r.setOwnerId(s.getOwnerId());
        r.setName(s.getName());
        r.setAnnouncement(s.getAnnouncement());
        r.setOnSaleCount(productRepository.countBySellerIdAndStatus(s.getOwnerId(), ProductStatus.ON_SALE));
        r.setSoldCount(orderRepository.countBySellerIdAndStatus(s.getOwnerId(), OrderStatus.COMPLETED));
        r.setRating(reviewRepository.avgSellerRating(s.getOwnerId()));
        r.setRatingCount(reviewRepository.countBySellerId(s.getOwnerId()));
        r.setCreatedAt(s.getCreatedAt());
        return r;
    }
}
