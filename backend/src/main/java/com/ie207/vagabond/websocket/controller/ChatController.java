package com.ie207.vagabond.websocket.controller;

import com.ie207.vagabond.dto.ChatMessage;
import com.ie207.vagabond.model.Message;
import com.ie207.vagabond.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @MessageMapping("/chat.sendToAdmin")
    public void sendMessageToAdmin(@Payload ChatMessage message) {
        chatService.sendMessage(
                message.getSenderId(),
                message.getReceiverId(),
                message.getText(),
                "toAdmin"
        );
    }

    @MessageMapping("/chat.sendToUser")
    public void sendMessageToUser(@Payload ChatMessage message) {
        chatService.sendMessage(
                message.getSenderId(),
                message.getReceiverId(),
                message.getText(),
                "toUser"
        );
    }
}
