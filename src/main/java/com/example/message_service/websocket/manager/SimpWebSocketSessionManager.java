package com.example.message_service.websocket.manager;

import java.util.Optional;

import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import com.example.message_service.dto.WebSocketSessionDTO;
import com.example.message_service.external.ExternalRoomService;
import com.example.message_service.external.dto.NewMemberResponse;
import com.example.message_service.jwt.JWTValidator;
import com.example.message_service.jwt.claims.JWTClaims;

@Component("simp")
public class SimpWebSocketSessionManager implements WebSocketSessionManager {

    private final JWTValidator jwtValidator;
    private final ExternalRoomService externalRoomService;

    public SimpWebSocketSessionManager(JWTValidator jwtValidator, ExternalRoomService externalRoomService) {
        this.jwtValidator = jwtValidator;
        this.externalRoomService = externalRoomService;
    }

    @Override
    public boolean register(StompHeaderAccessor accessor) {

        if (isRegistered(accessor)) {
            return false;
        }

        String token = accessor.getFirstNativeHeader("Authorization");

        Optional<JWTClaims> optionalJWT = jwtValidator.validateToken(token);

        String roomId = accessor.getDestination().substring(7);

        try {

            if (optionalJWT.isEmpty()) {

                fillGuestSession(roomId, accessor);

                return true;
            }

            fillAuthenticatedSession(optionalJWT.get(), roomId, accessor);

            return true;

        } catch (Exception e) {
            e.printStackTrace();

            return false;
        }
    }

    @Override
    public void remove(StompHeaderAccessor accessor) {

        WebSocketSessionDTO sessionDTO = getAuthenticatedUser(accessor);

        if (sessionDTO != null) {
            externalRoomService.removeMember(sessionDTO.getMemberId(), accessor.getSessionId());
        }

    }

    @Override
    public WebSocketSessionDTO getAuthenticatedUser(StompHeaderAccessor accessor) {
        WebSocketSessionDTO clientSession = (WebSocketSessionDTO) accessor.getSessionAttributes()
                .get("user");

        return clientSession;
    }

    @Override
    public boolean isRegistered(StompHeaderAccessor accessor) {
        WebSocketSessionDTO clientSession = (WebSocketSessionDTO) accessor.getSessionAttributes()
                .get("user");

        return clientSession != null;
    }

    @Override
    public boolean isGuest(StompHeaderAccessor accessor) {
        WebSocketSessionDTO clientSession = (WebSocketSessionDTO) accessor.getSessionAttributes()
                .get("user");

        return clientSession == null || clientSession.getUsername().equals(WebSocketSessionManager.GUEST_USERNAME);
    }

    private void fillAuthenticatedSession(JWTClaims claims, String roomId, StompHeaderAccessor accessor)
            throws RestClientException {

        WebSocketSessionDTO sessionDTO = new WebSocketSessionDTO();

        NewMemberResponse newMember = externalRoomService.addNewMember(claims, roomId, accessor.getSessionId());

        sessionDTO.setMemberId(newMember.getId());
        sessionDTO.setJoinedAt(newMember.getJoinedAt());
        sessionDTO.setRoomId(newMember.getRoomId());

        sessionDTO.setUserId(claims.getSub());
        sessionDTO.setUsername(claims.getUsername());
        sessionDTO.setProfilePicture(claims.getProfilePicture());

        accessor.getSessionAttributes().put("user", sessionDTO);

    }

    private void fillGuestSession(String roomId, StompHeaderAccessor accessor) {
        WebSocketSessionDTO sessionDTO = new WebSocketSessionDTO();

        sessionDTO.setUsername(WebSocketSessionManager.GUEST_USERNAME);
        sessionDTO.setRoomId(roomId);

        accessor.getSessionAttributes().put("user", sessionDTO);

    }

}
