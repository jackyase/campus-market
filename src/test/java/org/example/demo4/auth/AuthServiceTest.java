package org.example.demo4.auth;

import org.example.demo4.auth.dto.LoginRequest;
import org.example.demo4.auth.dto.RegisterRequest;
import org.example.demo4.common.BusinessException;
import org.example.demo4.security.JwtUtil;
import org.example.demo4.user.User;
import org.example.demo4.user.UserRepository;
import org.example.demo4.user.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_duplicateUsername_throws() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("alice");
        req.setPassword("123456");

        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已被注册");
    }

    @Test
    void register_success_nicknameDefaultsToUsername() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("alice");
        req.setPassword("123456");

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("encoded");

        authService.register(req);

        verify(userRepository).save(argThat(u -> "alice".equals(u.getNickname())));
    }

    @Test
    void login_wrongPassword_throws() {
        LoginRequest req = new LoginRequest();
        req.setUsername("alice");
        req.setPassword("wrong");

        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setPassword("encoded");
        user.setRole(UserRole.USER);

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户名或密码错误");
    }

    @Test
    void login_success_returnsToken() {
        LoginRequest req = new LoginRequest();
        req.setUsername("alice");
        req.setPassword("123456");

        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setPassword("encoded");
        user.setNickname("Alice");
        user.setRole(UserRole.USER);

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "encoded")).thenReturn(true);
        when(jwtUtil.generateToken(1L, "alice")).thenReturn("token-xyz");

        var resp = authService.login(req);

        assertThat(resp.getToken()).isEqualTo("token-xyz");
        assertThat(resp.getUsername()).isEqualTo("alice");
    }
}
