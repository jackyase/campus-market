package org.example.demo4.review;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_title", length = 100)
    private String productTitle;

    @Column(name = "product_rating", nullable = false)
    private Integer productRating;

    @Column(name = "seller_rating", nullable = false)
    private Integer sellerRating;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
