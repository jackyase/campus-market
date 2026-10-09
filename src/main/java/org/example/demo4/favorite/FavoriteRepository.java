package org.example.demo4.favorite;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByUserIdAndProductId(Long userId, Long productId);

    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    void deleteByUserId(Long userId);

    void deleteByProductIdIn(Collection<Long> productIds);
}
