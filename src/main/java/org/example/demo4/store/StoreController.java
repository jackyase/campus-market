package org.example.demo4.store;

import jakarta.validation.Valid;
import org.example.demo4.common.R;
import org.example.demo4.store.dto.StoreRequest;
import org.example.demo4.store.dto.StoreResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/store")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @PostMapping("/open")
    public R<StoreResponse> open(@Valid @RequestBody StoreRequest req) {
        return R.ok(storeService.open(req));
    }

    @GetMapping("/me")
    public R<StoreResponse> me() {
        return R.ok(storeService.getMyStore());
    }

    @PutMapping("/me")
    public R<StoreResponse> update(@Valid @RequestBody StoreRequest req) {
        return R.ok(storeService.updateStore(req));
    }

    @GetMapping("/{id}")
    public R<StoreResponse> get(@PathVariable Long id) {
        return R.ok(storeService.getStore(id));
    }

    @PostMapping("/close")
    public R<Void> close() {
        storeService.close();
        return R.ok();
    }
}
