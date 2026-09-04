package org.example.demo4.trade;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByBuyerIdOrderByCreatedAtDesc(Long buyerId, Pageable pageable);

    Page<Order> findByBuyerIdAndStatusOrderByCreatedAtDesc(Long buyerId, OrderStatus status, Pageable pageable);

    List<Order> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    long countBySellerIdAndStatus(Long sellerId, OrderStatus status);

    Optional<Order> findByIdAndBuyerId(Long id, Long buyerId);

    Optional<Order> findByIdAndSellerId(Long id, Long sellerId);

    @Query("select o.status, count(o) from Order o where o.buyerId = :buyerId group by o.status")
    List<Object[]> countGroupByStatus(@Param("buyerId") Long buyerId);
}
