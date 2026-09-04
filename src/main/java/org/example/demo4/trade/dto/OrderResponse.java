package org.example.demo4.trade.dto;

import lombok.Data;
import org.example.demo4.trade.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderResponse {
    private Long id;
    private String orderNo;
    private Long buyerId;
    private String buyerName;
    private Long sellerId;
    private String sellerName;
    private Long productId;
    private String productTitle;
    private String productImage;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String paymentMethod;
    private String transactionNo;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private boolean reviewed;
}
