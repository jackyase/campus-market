package org.example.demo4.user;

import jakarta.validation.Valid;
import org.example.demo4.common.BusinessException;
import org.example.demo4.common.R;
import org.example.demo4.product.ProductService;
import org.example.demo4.product.dto.ProductResponse;
import org.example.demo4.review.ReviewService;
import org.example.demo4.review.dto.ReviewResponse;
import org.example.demo4.user.dto.ProfileUpdateRequest;
import org.example.demo4.user.dto.PublicProfileResponse;
import org.example.demo4.user.dto.UserResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final ProductService productService;
    private final ReviewService reviewService;

    public UserController(UserRepository userRepository,
                          UserService userService,
                          ProductService productService,
                          ReviewService reviewService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.productService = productService;
        this.reviewService = reviewService;
    }

    @GetMapping("/user/me")
    public R<UserResponse> me(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));
        return R.ok(UserResponse.from(user));
    }

    @PutMapping("/user/profile")
    public R<UserResponse> updateProfile(@Valid @RequestBody ProfileUpdateRequest req) {
        return R.ok(UserResponse.from(userService.updateProfile(req)));
    }

    @GetMapping("/users/{id}")
    public R<PublicProfileResponse> publicProfile(@PathVariable Long id) {
        return R.ok(userService.publicProfile(id));
    }

    @GetMapping("/users/{id}/products")
    public R<List<ProductResponse>> userProducts(@PathVariable Long id) {
        return R.ok(productService.listBySeller(id));
    }

    @GetMapping("/users/{id}/reviews")
    public R<List<ReviewResponse>> userReviews(@PathVariable Long id) {
        return R.ok(reviewService.listBySeller(id));
    }

    @DeleteMapping("/user/account")
    public R<Void> deleteAccount() {
        userService.deleteAccount();
        return R.ok();
    }
}
