package org.example.demo4.store.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StoreResponse {
    private Long id;
    private Long ownerId;
    private String name;
    private String announcement;
    private Long onSaleCount;
    private Long soldCount;
    private Double rating;
    private Long ratingCount;
    private LocalDateTime createdAt;
}
