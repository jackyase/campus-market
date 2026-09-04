package org.example.demo4.trade.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CheckoutRequest {

    @NotEmpty(message = "请选择要结算的商品")
    private List<Long> cartItemIds;
}
