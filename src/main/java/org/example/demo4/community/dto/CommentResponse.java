package org.example.demo4.community.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentResponse {
    private Long id;
    private Long postId;
    private Long userId;
    private String userName;
    private String userAvatar;
    private String content;
    private boolean mine;
    private LocalDateTime createdAt;
}
