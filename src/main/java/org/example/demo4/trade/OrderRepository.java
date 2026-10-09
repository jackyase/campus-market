package org.example.demo4.trade;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :id and o.buyerId = :buyerId")
    Optional<Order> findByIdAndBuyerIdForUpdate(@Param("id") Long id, @Param("buyerId") Long buyerId);

    Optional<Order> findByIdAndSellerId(Long id, Long sellerId);

    @Query("select o.status, count(o) from Order o where o.buyerId = :buyerId group by o.status")
    List<Object[]> countGroupByStatus(@Param("buyerId") Long buyerId);

    void deleteByBuyerId(Long buyerId);

    void deleteBySellerId(Long sellerId);

    @Modifying
    @Query("update Order o set o.sellerName = :name where o.sellerId = :sellerId")
    int updateSellerNameBySellerId(@Param("name") String name, @Param("sellerId") Long sellerId);
}
