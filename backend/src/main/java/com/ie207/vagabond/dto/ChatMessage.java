package com.ie207.vagabond.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private String id;
    private String senderId;
    private String receiverId;
    private String text;
    private LocalDateTime timestamp;
    private String senderRole; // "admin" or "user"
}
