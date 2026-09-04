package org.example.demo4.message.dto;

import lombok.Data;
import org.example.demo4.message.Message;

import java.time.LocalDateTime;

@Data
public class MessageResponse {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    private boolean mine;
    private LocalDateTime createdAt;

    public static MessageResponse from(Message m, Long currentUserId) {
        MessageResponse r = new MessageResponse();
        r.setId(m.getId());
        r.setSenderId(m.getSenderId());
        r.setReceiverId(m.getReceiverId());
        r.setContent(m.getContent());
        r.setMine(m.getSenderId().equals(currentUserId));
        r.setCreatedAt(m.getCreatedAt());
        return r;
    }
}
