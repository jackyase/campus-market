package org.example.demo4.favorite;

import org.example.demo4.common.R;
import org.example.demo4.product.dto.ProductResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public R<List<ProductResponse>> list() {
        return R.ok(favoriteService.list());
    }

    @GetMapping("/ids")
    public R<Set<Long>> ids() {
        return R.ok(favoriteService.favoritedIds());
    }

    @PostMapping("/{productId}/toggle")
    public R<Boolean> toggle(@PathVariable Long productId) {
        return R.ok(favoriteService.toggle(productId));
    }
}
