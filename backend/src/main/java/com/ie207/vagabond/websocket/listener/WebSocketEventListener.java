package com.ie207.vagabond.websocket.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    private final SimpMessagingTemplate messagingTemplate;

    private final Map<String, String> userSocketMap = new ConcurrentHashMap<>();

    private final Set<String> adminSessions = ConcurrentHashMap.newKeySet();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        String userId = null;
        String role = null;

        GenericMessage<?> connectMessage = (GenericMessage<?>) headerAccessor.getHeader("simpConnectMessage");
        if (connectMessage != null) {

            Map<String, Object> connectHeaders = connectMessage.getHeaders();
            @SuppressWarnings("unchecked")
            Map<String, Object> sessionAttributes = (Map<String, Object>) connectHeaders.get("simpSessionAttributes");

            if (sessionAttributes != null) {
                userId = (String) sessionAttributes.get("userId");
                role = (String) sessionAttributes.get("role");
                System.out.println("✅ Got from simpSessionAttributes: userId=" + userId + ", role=" + role);
            }
        }

        if (userId != null) {
            System.out.println("✅ User connected: sessionId=" + sessionId + ", userId=" + userId + ", role=" + role);

            // Lưu vào maps
            userSocketMap.put(userId, sessionId);

            if ("admin".equalsIgnoreCase(role) || "ADMIN".equals(role)) {
                adminSessions.add(sessionId);
                System.out.println("✅ Added to admin sessions");
            }

            sendOnlineUsersToAdmins();
        } else {
            System.out.println("❌ Cannot get userId");
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        System.out.println("A user disconnected: " + sessionId);

        String disconnectedUserId = null;
        for (Map.Entry<String, String> entry : userSocketMap.entrySet()) {
            if (entry.getValue().equals(sessionId)) {
                disconnectedUserId = entry.getKey();
                break;
            }
        }

        if (disconnectedUserId != null) {
            userSocketMap.remove(disconnectedUserId);
        }

        adminSessions.remove(sessionId);
        sendOnlineUsersToAdmins();
    }

    private void sendOnlineUsersToAdmins() {
        Set<String> onlineUserIds = userSocketMap.keySet();

        messagingTemplate.convertAndSend(
                "/topic/admin/onlineUsers",
                onlineUserIds
        );
    }

    public String getReceiverSocketId(String userId) {
        return userSocketMap.get(userId);
    }

    public Map<String, String> getUserSocketMap() {
        return new HashMap<>(userSocketMap);
    }
}
