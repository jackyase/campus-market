package org.example.demo4.product.dto;

import lombok.Data;
import org.example.demo4.product.ProductCondition;
import org.example.demo4.product.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductResponse {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private ProductCondition condition;
    private ProductStatus status;
    private Integer stock;
    private Integer viewCount;
    private Long categoryId;
    private String categoryName;
    private Long sellerId;
    private String sellerName;
    private List<String> images;
    private LocalDateTime createdAt;
}
