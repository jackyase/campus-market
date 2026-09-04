package org.example.demo4.message;

import jakarta.validation.Valid;
import org.example.demo4.common.R;
import org.example.demo4.message.dto.ConversationResponse;
import org.example.demo4.message.dto.MessageResponse;
import org.example.demo4.message.dto.SendMessageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/conversations")
    public R<List<ConversationResponse>> conversations() {
        return R.ok(messageService.listConversations());
    }

    @GetMapping("/with/{otherUserId}")
    public R<List<MessageResponse>> history(@PathVariable Long otherUserId) {
        return R.ok(messageService.history(otherUserId));
    }

    @PostMapping
    public R<MessageResponse> send(@Valid @RequestBody SendMessageRequest req) {
        return R.ok(messageService.send(req.getReceiverId(), req.getContent()));
    }

    @GetMapping("/unread-count")
    public R<Long> unreadCount() {
        return R.ok(messageService.unreadCount());
    }
}
