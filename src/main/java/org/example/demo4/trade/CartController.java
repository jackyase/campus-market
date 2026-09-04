package org.example.demo4.trade;

import org.example.demo4.common.R;
import org.example.demo4.trade.dto.CartItemResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final TradeService tradeService;

    public CartController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @GetMapping
    public R<List<CartItemResponse>> list() {
        return R.ok(tradeService.listCart());
    }

    @PostMapping
    public R<Void> add(@RequestParam Long productId,
                       @RequestParam(defaultValue = "1") Integer quantity) {
        tradeService.addToCart(productId, quantity);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestParam Integer quantity) {
        tradeService.updateQuantity(id, quantity);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        tradeService.removeItem(id);
        return R.ok();
    }
}
