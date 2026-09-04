package org.example.demo4.product;

import jakarta.validation.Valid;
import org.example.demo4.common.BusinessException;
import org.example.demo4.common.PageResult;
import org.example.demo4.common.R;
import org.example.demo4.product.dto.ProductRequest;
import org.example.demo4.product.dto.ProductResponse;
import org.example.demo4.user.User;
import org.example.demo4.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final UserRepository userRepository;

    public ProductController(ProductService productService, UserRepository userRepository) {
        this.productService = productService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public R<PageResult<ProductResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) ProductCondition condition,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return R.ok(productService.list(keyword, categoryId, condition,
                minPrice, maxPrice, sort, page, size));
    }

    @GetMapping("/{id}")
    public R<ProductResponse> detail(@PathVariable Long id) {
        return R.ok(productService.detail(id));
    }

    @PostMapping
    public R<ProductResponse> create(@Valid @RequestBody ProductRequest req, Authentication authentication) {
        User seller = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));
        return R.ok(productService.create(req, seller));
    }

    @GetMapping("/mine")
    public R<List<ProductResponse>> mine(Authentication authentication) {
        User seller = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));
        return R.ok(productService.listMine(seller.getId()));
    }

    @PutMapping("/{id}/status")
    public R<ProductResponse> updateStatus(@PathVariable Long id,
                                           @RequestParam ProductStatus status,
                                           Authentication authentication) {
        User seller = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));
        return R.ok(productService.updateStatus(id, status, seller.getId()));
    }
}
