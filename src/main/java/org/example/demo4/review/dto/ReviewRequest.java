package org.example.demo4.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRequest {

    @NotNull(message = "订单不能为空")
    private Long orderId;

    @NotNull(message = "请给商品打分")
    @Min(value = 1, message = "评分在 1-5 之间")
    @Max(value = 5, message = "评分在 1-5 之间")
    private Integer productRating;

    @NotNull(message = "请给卖家打分")
    @Min(value = 1, message = "评分在 1-5 之间")
    @Max(value = 5, message = "评分在 1-5 之间")
    private Integer sellerRating;

    private String content;
}
