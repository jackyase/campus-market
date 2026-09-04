package org.example.demo4.message.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationResponse {
    private Long otherUserId;
    private String otherUserName;
    private String otherUserAvatar;
    private String lastMessage;
    private LocalDateTime lastTime;
    private long unreadCount;
}
