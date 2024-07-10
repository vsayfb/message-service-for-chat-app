package com.example.message_service.websocket.manager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.message_service.external.ExternalRoomService;
import com.example.message_service.jwt.JWTValidator;

@Configuration
public class WebSocketSessionManagerConfig {

    private final JWTValidator jwtValidator;
    private final ExternalRoomService externalRoomService;

    public WebSocketSessionManagerConfig(JWTValidator jwtValidator, ExternalRoomService externalRoomService) {
        this.jwtValidator = jwtValidator;
        this.externalRoomService = externalRoomService;
    }

    @Bean
    WebSocketSessionManager webSocketSessionManager() {
        return new SimpWebSocketSessionManager(jwtValidator, externalRoomService);
    }
}
