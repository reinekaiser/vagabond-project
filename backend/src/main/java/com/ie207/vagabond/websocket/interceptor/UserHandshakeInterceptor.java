package com.ie207.vagabond.websocket.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

@Component
public class UserHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            HttpServletRequest httpRequest = servletRequest.getServletRequest();

            // Lấy userId và role từ query parameters
            String userId = httpRequest.getParameter("userId");
            String role = httpRequest.getParameter("role");

            System.out.println("WebSocket Handshake - userId: " + userId + ", role: " + role);

            if (userId != null) {
                attributes.put("userId", userId);
                attributes.put("role", role != null ? role : "user");
                Principal principal = () -> userId;
                Authentication auth = new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                attributes.put("authentication", auth);

                System.out.println("attributes" + attributes);
            }
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // Không cần xử lý gì
    }
}
