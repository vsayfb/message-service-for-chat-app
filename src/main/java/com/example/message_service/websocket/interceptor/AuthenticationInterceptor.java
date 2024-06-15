package com.example.message_service.websocket.interceptor;


import com.example.message_service.jwt.JWTSigner;
import com.example.message_service.jwt.claims.JWTClaims;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthenticationInterceptor implements ChannelInterceptor {

    private final JWTSigner jwtSigner;

    public AuthenticationInterceptor(JWTSigner jwtSigner) {
        this.jwtSigner = jwtSigner;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.SEND.equals(headerAccessor.getCommand())) {

            String dest = headerAccessor.getDestination();

            if (dest == null || !dest.startsWith("/topic/")) {
                return null;
            }

            String roomId = dest.substring(7);

            if (roomId.isEmpty()) {
                return null;
            }

            String token = headerAccessor.getFirstNativeHeader("Authorization");

            if (token == null || !token.startsWith("Bearer ")) {
                return null;
            }

            Optional<JWTClaims> jwtClaims = jwtSigner.validateToken(token.substring(7));

            if (jwtClaims.isEmpty()) {
                return null;
            }

            JWTClaims claims = jwtClaims.get();

            if (!claims.getRoomId().equals(roomId)) {
                return null;
            }

        }

        return message;
    }
}
