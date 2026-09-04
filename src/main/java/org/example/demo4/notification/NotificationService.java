package org.example.demo4.notification;

import org.example.demo4.common.BusinessException;
import org.example.demo4.common.CurrentUserService;
import org.example.demo4.common.PageResult;
import org.example.demo4.notification.dto.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    public static final String TYPE_ORDER = "ORDER";
    public static final String TYPE_SHIPPED = "SHIPPED";
    public static final String TYPE_COMPLETED = "COMPLETED";
    public static final String TYPE_REVIEW = "REVIEW";

    private final NotificationRepository notificationRepository;
    private final CurrentUserService currentUserService;

    public NotificationService(NotificationRepository notificationRepository,
                               CurrentUserService currentUserService) {
        this.notificationRepository = notificationRepository;
        this.currentUserService = currentUserService;
    }

    /** 发送通知（供交易/评价等服务调用，参与调用方事务） */
    @Transactional
    public void notify(Long userId, String type, String content, Long relatedId) {
        if (userId == null) {
            return;
        }
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setContent(content);
        n.setRelatedId(relatedId);
        notificationRepository.save(n);
    }

    public PageResult<NotificationResponse> list(int page, int size) {
        Long userId = currentUserService.requireUser().getId();
        Page<Notification> p = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50)));
        return new PageResult<>(p.getContent().stream().map(NotificationResponse::from).toList(),
                p.getTotalElements(), page, size);
    }

    public long unreadCount() {
        Long userId = currentUserService.requireUser().getId();
        return notificationRepository.countUnread(userId);
    }

    @Transactional
    public void markRead(Long id) {
        Long userId = currentUserService.requireUser().getId();
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "通知不存在"));
        if (!n.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作该通知");
        }
        n.setRead(true);
        notificationRepository.save(n);
    }

    @Transactional
    public void markAllRead() {
        Long userId = currentUserService.requireUser().getId();
        notificationRepository.markAllRead(userId);
    }
}
