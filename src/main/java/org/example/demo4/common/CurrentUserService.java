package org.example.demo4.common;

import org.example.demo4.user.User;
import org.example.demo4.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User requireUser() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || a.getName() == null) {
            throw new BusinessException(401, "未登录");
        }
        return userRepository.findByUsername(a.getName())
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));
    }

    public Long optionalUserId() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated() || "anonymousUser".equals(a.getName())) {
            return null;
        }
        return userRepository.findByUsername(a.getName()).map(User::getId).orElse(null);
    }
}
