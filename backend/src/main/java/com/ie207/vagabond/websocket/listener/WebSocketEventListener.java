package com.ie207.vagabond.websocket.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes != null) {
            String userId = (String) sessionAttributes.get("userId");
            String role = (String) sessionAttributes.get("role");

            System.out.println("A user connected at " + sessionId + ": " + userId + " with role: " + role);

            if (userId != null) {
                userSocketMap.put(userId, sessionId);

                if ("admin".equals(role)) {
                    adminSessions.add(sessionId);
                }
            }

            sendOnlineUsersToAdmins();
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

        for (String adminSessionId : adminSessions) {
            messagingTemplate.convertAndSendToUser(
                    adminSessionId,
                    "/queue/onlineUsers",
                    onlineUserIds
            );
        }
    }

    public String getReceiverSocketId(String userId) {
        return userSocketMap.get(userId);
    }

    public Map<String, String> getUserSocketMap() {
        return new HashMap<>(userSocketMap);
    }
}
