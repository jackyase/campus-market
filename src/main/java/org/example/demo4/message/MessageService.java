package org.example.demo4.message;

import org.example.demo4.common.BusinessException;
import org.example.demo4.common.CurrentUserService;
import org.example.demo4.message.dto.ConversationResponse;
import org.example.demo4.message.dto.MessageResponse;
import org.example.demo4.user.User;
import org.example.demo4.user.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public MessageService(MessageRepository messageRepository,
                          UserRepository userRepository,
                          CurrentUserService currentUserService) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    public List<ConversationResponse> listConversations() {
        Long me = currentUserService.requireUser().getId();
        List<Message> recent = messageRepository.findRecentByUser(me, PageRequest.of(0, 200));
        if (recent.isEmpty()) {
            return List.of();
        }
        // 每个「对方」取最近一条（recent 已按时间倒序）
        Map<Long, Message> lastByOther = new LinkedHashMap<>();
        for (Message m : recent) {
            Long other = m.getSenderId().equals(me) ? m.getReceiverId() : m.getSenderId();
            lastByOther.putIfAbsent(other, m);
        }
        Map<Long, Long> unreadByOther = messageRepository.countUnreadBySender(me).stream()
                .collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));

        Set<Long> otherIds = lastByOther.keySet();
        Map<Long, User> users = userRepository.findAllById(otherIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return lastByOther.entrySet().stream().map(e -> {
            Long otherId = e.getKey();
            Message last = e.getValue();
            User u = users.get(otherId);
            ConversationResponse r = new ConversationResponse();
            r.setOtherUserId(otherId);
            r.setOtherUserName(u == null ? "用户" + otherId : displayName(u));
            r.setOtherUserAvatar(u == null ? null : u.getAvatar());
            r.setLastMessage(last.getContent());
            r.setLastTime(last.getCreatedAt());
            r.setUnreadCount(unreadByOther.getOrDefault(otherId, 0L));
            return r;
        }).collect(Collectors.toList());
    }

    @Transactional
    public List<MessageResponse> history(Long otherUserId) {
        Long me = currentUserService.requireUser().getId();
        if (me.equals(otherUserId)) {
            throw new BusinessException("不能和自己私信");
        }
        List<Message> list = messageRepository.findConversation(me, otherUserId);
        messageRepository.markConversationRead(me, otherUserId);
        return list.stream().map(m -> MessageResponse.from(m, me)).collect(Collectors.toList());
    }

    @Transactional
    public MessageResponse send(Long receiverId, String content) {
        User me = currentUserService.requireUser();
        if (me.getId().equals(receiverId)) {
            throw new BusinessException("不能和自己私信");
        }
        if (!userRepository.existsById(receiverId)) {
            throw new BusinessException(404, "用户不存在");
        }
        Message m = new Message();
        m.setSenderId(me.getId());
        m.setReceiverId(receiverId);
        m.setContent(content);
        messageRepository.save(m);
        return MessageResponse.from(m, me.getId());
    }

    public long unreadCount() {
        Long me = currentUserService.requireUser().getId();
        return messageRepository.countUnread(me);
    }

    private String displayName(User u) {
        return u.getNickname() == null || u.getNickname().isBlank() ? u.getUsername() : u.getNickname();
    }
}
