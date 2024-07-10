package com.example.message_service.websocket.manager;

import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import com.example.message_service.dto.WebSocketSessionDTO;

public interface WebSocketSessionManager {

    String GUEST_USERNAME = "guest-user-not-authenticated";

    boolean register(StompHeaderAccessor accessor);

    void remove(StompHeaderAccessor accessor);

    WebSocketSessionDTO getAuthenticatedUser(StompHeaderAccessor accessor);

    boolean isRegistered(StompHeaderAccessor accessor);

    boolean isGuest(StompHeaderAccessor accessor);

    boolean isAuthenticated(StompHeaderAccessor accessor);
}
