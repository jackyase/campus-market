package org.example.demo4.bargain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BargainRequest {

    @NotNull(message = "商品不能为空")
    private Long productId;

    @NotNull(message = "出价不能为空")
    @DecimalMin(value = "0.01", message = "出价必须大于 0")
    private BigDecimal price;
}
