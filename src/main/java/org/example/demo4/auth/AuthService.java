package org.example.demo4.auth;

import org.example.demo4.auth.dto.LoginRequest;
import org.example.demo4.auth.dto.LoginResponse;
import org.example.demo4.auth.dto.RegisterRequest;
import org.example.demo4.common.BusinessException;
import org.example.demo4.security.JwtUtil;
import org.example.demo4.user.User;
import org.example.demo4.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public void register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new BusinessException("用户名已被注册");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getNickname() == null || req.getNickname().isBlank()
                ? req.getUsername() : req.getNickname());
        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new LoginResponse(token, user.getId(), user.getUsername(),
                user.getNickname(), user.getRole().name());
    }
}
