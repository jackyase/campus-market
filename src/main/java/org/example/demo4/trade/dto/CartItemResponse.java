package org.example.demo4.trade.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemResponse {
    private Long id;
    private Long productId;
    private String title;
    private String image;
    private BigDecimal price;
    private Integer quantity;
    private Integer stock;
    private String sellerName;
    private String condition;
    private boolean available;
}
