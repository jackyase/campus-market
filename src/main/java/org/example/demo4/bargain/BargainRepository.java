package org.example.demo4.bargain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BargainRepository extends JpaRepository<Bargain, Long> {
    boolean existsByProductIdAndBuyerIdAndStatus(Long productId, Long buyerId, BargainStatus status);

    List<Bargain> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    List<Bargain> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    Optional<Bargain> findByIdAndSellerId(Long id, Long sellerId);
}
