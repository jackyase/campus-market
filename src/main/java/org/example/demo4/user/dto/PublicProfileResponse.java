package org.example.demo4.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PublicProfileResponse {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String cover;
    private String bio;
    private String gender;
    private String school;
    private String college;
    private String interests;
    private Double sellerRating;
    private Long sellerRatingCount;
    private Long onSaleCount;
    private Long followerCount;
    private Long followingCount;
    private boolean following;
    private String storeName;
    private String storeAnnouncement;
    private LocalDateTime createdAt;
}
