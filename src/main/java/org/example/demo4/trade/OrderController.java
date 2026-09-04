package org.example.demo4.trade;

import jakarta.validation.Valid;
import org.example.demo4.common.PageResult;
import org.example.demo4.common.R;
import org.example.demo4.trade.dto.CheckoutRequest;
import org.example.demo4.trade.dto.OrderCreateRequest;
import org.example.demo4.trade.dto.OrderResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final TradeService tradeService;

    public OrderController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping
    public R<OrderResponse> create(@Valid @RequestBody OrderCreateRequest req) {
        return R.ok(tradeService.createOrder(req.getProductId(), req.getQuantity()));
    }

    @PostMapping("/checkout")
    public R<List<OrderResponse>> checkout(@Valid @RequestBody CheckoutRequest req) {
        return R.ok(tradeService.checkout(req.getCartItemIds()));
    }

    @GetMapping
    public R<PageResult<OrderResponse>> list(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return R.ok(tradeService.listOrders(status, page, size));
    }

    @GetMapping("/counts")
    public R<Map<String, Long>> counts() {
        return R.ok(tradeService.statusCounts());
    }

    @GetMapping("/sold")
    public R<List<OrderResponse>> sold() {
        return R.ok(tradeService.listSoldOrders());
    }

    @GetMapping("/{id}")
    public R<OrderResponse> detail(@PathVariable Long id) {
        return R.ok(tradeService.orderDetail(id));
    }

    @PostMapping("/{id}/pay")
    public R<OrderResponse> pay(@PathVariable Long id,
                                @RequestParam(required = false, defaultValue = "MOCK") String method) {
        return R.ok(tradeService.pay(id, method));
    }

    @PostMapping("/{id}/cancel")
    public R<OrderResponse> cancel(@PathVariable Long id) {
        return R.ok(tradeService.cancel(id));
    }

    @PostMapping("/{id}/ship")
    public R<OrderResponse> ship(@PathVariable Long id) {
        return R.ok(tradeService.ship(id));
    }

    @PostMapping("/{id}/confirm")
    public R<OrderResponse> confirm(@PathVariable Long id) {
        return R.ok(tradeService.confirm(id));
    }
}
