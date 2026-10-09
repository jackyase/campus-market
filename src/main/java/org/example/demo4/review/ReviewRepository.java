package org.example.demo4.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);

    List<Review> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    boolean existsByOrderId(Long orderId);

    List<Review> findByOrderIdIn(Collection<Long> orderIds);

    long countBySellerId(Long sellerId);

    @Query("select avg(r.sellerRating) from Review r where r.sellerId = :sellerId")
    Double avgSellerRating(@Param("sellerId") Long sellerId);

    void deleteByReviewerId(Long reviewerId);

    void deleteBySellerId(Long sellerId);
}
