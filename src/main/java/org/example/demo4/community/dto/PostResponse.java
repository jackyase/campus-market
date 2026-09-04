package org.example.demo4.community.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostResponse {
    private Long id;
    private Long userId;
    private String authorName;
    private String authorAvatar;
    private String content;
    private List<String> topics;
    private List<PostMediaResponse> media;
    private long likeCount;
    private long commentCount;
    private boolean liked;
    private boolean mine;
    private LocalDateTime createdAt;
}
