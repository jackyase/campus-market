package org.example.demo4.review.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private Long id;
    private Long reviewerId;
    private String reviewerName;
    private String reviewerAvatar;
    private Long productId;
    private String productTitle;
    private Integer productRating;
    private Integer sellerRating;
    private String content;
    private LocalDateTime createdAt;
}
