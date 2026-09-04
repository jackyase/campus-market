package org.example.demo4.community.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PostCreateRequest {

    @NotBlank(message = "内容不能为空")
    @Size(max = 2000, message = "内容过长")
    private String content;

    private List<String> topics;

    @Valid
    private List<PostMediaRequest> media;

    @Data
    public static class PostMediaRequest {
        private String url;
        private String type;
    }
}
