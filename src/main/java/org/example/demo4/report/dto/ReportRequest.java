package org.example.demo4.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportRequest {

    /** PRODUCT / POST */
    @NotBlank(message = "举报类型不能为空")
    private String targetType;

    @NotNull(message = "举报对象不能为空")
    private Long targetId;

    @NotBlank(message = "请填写举报原因")
    private String reason;
}
