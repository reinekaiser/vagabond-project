package com.ie207.vagabond.websocket.controller;

import com.ie207.vagabond.dto.ChatMessage;
import com.ie207.vagabond.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {
    private final ChatService chatService;

    @MessageMapping("/chat.sendToAdmin")
    public void sendMessageToAdmin(@Payload ChatMessage message) {
        log.info("📨 Received message to Admin - Sender: {}, Receiver: {}, Text: {}",
                message.getSenderId(), message.getReceiverId(), message.getText());
        chatService.sendMessage(
                message.getSenderId(),
                message.getReceiverId(),
                message.getText()
        );
    }

    @MessageMapping("/chat.sendToUser")
    public void sendMessageToUser(@Payload ChatMessage message) {
        log.info("📨 Received message to Admin - Sender: {}, Receiver: {}, Text: {}",
                message.getSenderId(), message.getReceiverId(), message.getText());
        chatService.sendMessage(
                message.getSenderId(),
                message.getReceiverId(),
                message.getText()
        );
    }
}
