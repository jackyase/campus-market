package org.example.demo4.auth;

import jakarta.validation.Valid;
import org.example.demo4.auth.dto.LoginRequest;
import org.example.demo4.auth.dto.LoginResponse;
import org.example.demo4.auth.dto.RegisterRequest;
import org.example.demo4.common.R;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public R<Void> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return R.ok();
    }

    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return R.ok(authService.login(req));
    }
}
