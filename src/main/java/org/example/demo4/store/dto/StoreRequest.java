package org.example.demo4.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StoreRequest {

    @NotBlank(message = "店铺名不能为空")
    @Size(max = 100, message = "店铺名过长")
    private String name;

    private String announcement;
}
