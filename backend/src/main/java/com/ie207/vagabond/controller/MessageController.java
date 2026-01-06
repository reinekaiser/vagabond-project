package com.ie207.vagabond.controller;

import com.ie207.vagabond.exception.ReceiverNotFound;
import com.ie207.vagabond.exception.SenderNotFound;
import com.ie207.vagabond.model.Message;
import com.ie207.vagabond.model.User;
import com.ie207.vagabond.request.SendMessageRequest;
import com.ie207.vagabond.response.UserWithMessageResponse;
import com.ie207.vagabond.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("api/messages")
@RequiredArgsConstructor
public class MessageController {
    private final ChatService chatService;

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@AuthenticationPrincipal String currentUserId)  {
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Something went wrong...");
        }

        List<User> users = chatService.getAllUsers(currentUserId);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/chat")
    public ResponseEntity<?> getUsersToChat(@AuthenticationPrincipal String currentUserId) {
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Something went wrong...");
        }
        List<UserWithMessageResponse> users = chatService.getUsersToChat(currentUserId);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/read/{id}")
    public ResponseEntity<?> markMessageAsRead(@AuthenticationPrincipal String currentUserId, @PathVariable String id) throws SenderNotFound, ReceiverNotFound {
        chatService.markMessageAsRead(currentUserId, id);
        return ResponseEntity.ok(Map.of("message", "Messages marked as read"));
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getChatHistory(@AuthenticationPrincipal String currentUserId, @PathVariable String userId) throws SenderNotFound, ReceiverNotFound{
        List<Message> messages = chatService.getChatHistory(currentUserId, userId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/send/{id}")
    public ResponseEntity<?> sendMessage(
            @AuthenticationPrincipal String currentUserId,
            @PathVariable String id,
            @RequestBody SendMessageRequest request) throws SenderNotFound, ReceiverNotFound {
        Message response = chatService.sendMessage(
                currentUserId,
                id,
                request.getText()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
