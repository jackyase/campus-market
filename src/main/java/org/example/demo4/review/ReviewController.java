package org.example.demo4.review;

import jakarta.validation.Valid;
import org.example.demo4.common.R;
import org.example.demo4.review.dto.ReviewRequest;
import org.example.demo4.review.dto.ReviewResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public R<ReviewResponse> create(@Valid @RequestBody ReviewRequest req) {
        return R.ok(reviewService.create(req));
    }

    @GetMapping("/product/{productId}")
    public R<List<ReviewResponse>> productReviews(@PathVariable Long productId) {
        return R.ok(reviewService.listByProduct(productId));
    }

    @GetMapping("/seller/{sellerId}")
    public R<List<ReviewResponse>> sellerReviews(@PathVariable Long sellerId) {
        return R.ok(reviewService.listBySeller(sellerId));
    }
}
