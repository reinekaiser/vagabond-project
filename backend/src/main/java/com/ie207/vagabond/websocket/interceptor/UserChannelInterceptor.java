package com.ie207.vagabond.websocket.interceptor;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null) {
                Authentication auth = (Authentication) sessionAttributes.get("authentication");
                if (auth != null) {
                    accessor.setUser(auth);
                    System.out.println("✅ STOMP CONNECTED as user: " + auth.getName());
                } else {
                    System.out.println("⚠️ No authentication found in session attributes");
                }
            }
        }
        return message;
    }
}