package com.example.message_service.websocket.interceptor;

import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import com.example.message_service.websocket.manager.WebSocketSessionManager;

@Component
public class AuthorizationInterceptor implements ChannelInterceptor {

    private final WebSocketSessionManager webSocketSessionManager;

    public AuthorizationInterceptor(@Qualifier("simp") WebSocketSessionManager webSocketSessionManager) {
        this.webSocketSessionManager = webSocketSessionManager;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.SEND.equals(accessor.getCommand())) {

            if (!accessor.getDestination().startsWith("/messages/")) {
                return null;
            }

            if (webSocketSessionManager.isGuest(accessor)) {
                return null;
            }

        }

        return message;
    }

}
