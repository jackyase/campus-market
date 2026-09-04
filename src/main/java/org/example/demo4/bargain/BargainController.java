package org.example.demo4.bargain;

import jakarta.validation.Valid;
import org.example.demo4.bargain.dto.BargainRequest;
import org.example.demo4.bargain.dto.BargainResponse;
import org.example.demo4.common.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bargains")
public class BargainController {

    private final BargainService bargainService;

    public BargainController(BargainService bargainService) {
        this.bargainService = bargainService;
    }

    @PostMapping
    public R<BargainResponse> create(@Valid @RequestBody BargainRequest req) {
        return R.ok(bargainService.create(req.getProductId(), req.getPrice()));
    }

    @GetMapping("/mine")
    public R<List<BargainResponse>> mine() {
        return R.ok(bargainService.listMine());
    }

    @GetMapping("/incoming")
    public R<List<BargainResponse>> incoming() {
        return R.ok(bargainService.listIncoming());
    }

    @PostMapping("/{id}/accept")
    public R<BargainResponse> accept(@PathVariable Long id) {
        return R.ok(bargainService.accept(id));
    }

    @PostMapping("/{id}/reject")
    public R<BargainResponse> reject(@PathVariable Long id) {
        return R.ok(bargainService.reject(id));
    }
}
