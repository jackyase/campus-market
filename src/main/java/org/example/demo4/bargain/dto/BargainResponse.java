package org.example.demo4.bargain.dto;

import lombok.Data;
import org.example.demo4.bargain.BargainStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BargainResponse {
    private Long id;
    private Long productId;
    private String productTitle;
    private Long buyerId;
    private String buyerName;
    private Long sellerId;
    private BigDecimal price;
    private BargainStatus status;
    private LocalDateTime createdAt;
}
