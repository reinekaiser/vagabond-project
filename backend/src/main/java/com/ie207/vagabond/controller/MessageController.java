package com.ie207.vagabond.controller;

import com.ie207.vagabond.exception.ReceiverNotFound;
import com.ie207.vagabond.exception.SenderNotFound;
import com.ie207.vagabond.model.Message;
import com.ie207.vagabond.model.User;
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
    public ResponseEntity<?> getAllUsers(@AuthenticationPrincipal User currentUser)  {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Something went wrong...");
        }

        List<User> users = chatService.getAllUsers(currentUser);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/chat")
    public ResponseEntity<?> getUsersToChat(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Something went wrong...");
        }
        List<UserWithMessageResponse> users = chatService.getUsersToChat(currentUser);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/read/{id}")
    public ResponseEntity<?> markMessageAsRead(@AuthenticationPrincipal User currentUser, @PathVariable String id) throws SenderNotFound, ReceiverNotFound {
        chatService.markMessageAsRead(currentUser.get_id(), id);
        return ResponseEntity.ok(Map.of("message", "Messages marked as read"));
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getChatHistory(@AuthenticationPrincipal User currentUser, @PathVariable String userId){
        List<Message> messages = chatService.getChatHistory(currentUser.get_id(), userId);
        return ResponseEntity.ok(messages);
    }
}
