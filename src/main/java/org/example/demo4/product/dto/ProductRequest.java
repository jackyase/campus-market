package org.example.demo4.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.example.demo4.product.ProductCondition;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题过长")
    private String title;

    @NotNull(message = "请选择分类")
    private Long categoryId;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于 0")
    private BigDecimal price;

    private BigDecimal originalPrice;

    @NotNull(message = "请选择成色")
    private ProductCondition condition;

    private String description;

    private Integer stock;

    private List<String> imageUrls;
}
