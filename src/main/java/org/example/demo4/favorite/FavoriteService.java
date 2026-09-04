package org.example.demo4.favorite;

import org.example.demo4.common.BusinessException;
import org.example.demo4.common.CurrentUserService;
import org.example.demo4.product.ProductRepository;
import org.example.demo4.product.ProductService;
import org.example.demo4.product.dto.ProductResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final CurrentUserService currentUserService;

    public FavoriteService(FavoriteRepository favoriteRepository,
                           ProductRepository productRepository,
                           ProductService productService,
                           CurrentUserService currentUserService) {
        this.favoriteRepository = favoriteRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public boolean toggle(Long productId) {
        Long userId = currentUserService.requireUser().getId();
        if (!productRepository.existsById(productId)) {
            throw new BusinessException(404, "商品不存在");
        }
        Optional<Favorite> existing = favoriteRepository.findByUserIdAndProductId(userId, productId);
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return false;
        }
        Favorite f = new Favorite();
        f.setUserId(userId);
        f.setProductId(productId);
        favoriteRepository.save(f);
        return true;
    }

    public List<ProductResponse> list() {
        Long userId = currentUserService.requireUser().getId();
        List<Long> ids = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(Favorite::getProductId)
                .collect(Collectors.toList());
        return productService.listByIds(ids);
    }

    /** 当前用户收藏的商品 id 集合（未登录返回空），用于页面展示收藏状态 */
    public Set<Long> favoritedIds() {
        Long userId = currentUserService.optionalUserId();
        if (userId == null) {
            return Set.of();
        }
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(Favorite::getProductId)
                .collect(Collectors.toSet());
    }
}
