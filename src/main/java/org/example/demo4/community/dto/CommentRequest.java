package org.example.demo4.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentRequest {

    @NotBlank(message = "评论不能为空")
    @Size(max = 500, message = "评论过长")
    private String content;
}
