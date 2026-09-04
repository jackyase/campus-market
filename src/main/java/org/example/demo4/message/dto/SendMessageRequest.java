package org.example.demo4.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendMessageRequest {

    @NotNull(message = "接收人不能为空")
    private Long receiverId;

    @NotBlank(message = "消息不能为空")
    private String content;
}
