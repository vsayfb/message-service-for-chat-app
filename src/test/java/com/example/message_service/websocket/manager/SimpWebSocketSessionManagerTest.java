package com.example.message_service.websocket.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import com.example.message_service.dto.WebSocketSessionDTO;
import com.example.message_service.external.ExternalRoomService;
import com.example.message_service.external.dto.NewMemberResponse;
import com.example.message_service.jwt.JWTValidator;
import com.example.message_service.jwt.claims.JWTClaims;

@ExtendWith(MockitoExtension.class)
public class SimpWebSocketSessionManagerTest {

    @Mock
    JWTValidator jwtValidator;

    @Mock
    ExternalRoomService externalRoomService;

    @InjectMocks
    SimpWebSocketSessionManager simpWebSocketSessionManager;

    @Nested
    class Register {

        @Test
        void shouldNotRegisterClientIfAlreadyRegistered() {

            String roomId = "123456";

            StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);

            headerAccessor.setDestination("/topic/" + roomId);
            headerAccessor.setSessionAttributes(new HashMap<>());

            WebSocketSessionDTO sessionDTO = new WebSocketSessionDTO();

            sessionDTO.setUsername("walter");

            headerAccessor.getSessionAttributes().put("user", sessionDTO);

            assertFalse(simpWebSocketSessionManager.register(headerAccessor));

        }

        @Test
        void shouldRegisterAsGuestUserIfJWTInvalid() {

            String roomId = "123456";

            StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);

            headerAccessor.setDestination("/topic/" + roomId);
            headerAccessor.setNativeHeader("Authorization", "ey");
            headerAccessor.setSessionAttributes(new HashMap<>());

            assertTrue(simpWebSocketSessionManager.register(headerAccessor));

            Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();

            assertNotNull(sessionAttributes.get("user"));

            WebSocketSessionDTO sessionDTO = (WebSocketSessionDTO) sessionAttributes.get("user");

            assertEquals(WebSocketSessionManager.GUEST_USERNAME, sessionDTO.getUsername());
            assertEquals(roomId, sessionDTO.getRoomId());
        }

        @Test
        void shouldRegisterAsMemberIfJWTValid() {

            String roomId = "123456";

            StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);

            headerAccessor.setDestination("/topic/" + roomId);
            headerAccessor.setNativeHeader("Authorization", "ey");
            headerAccessor.setSessionAttributes(new HashMap<>());
            headerAccessor.setSessionId("1");

            JWTClaims claims = new JWTClaims();

            claims.setSub("1");

            when(jwtValidator.validateToken(anyString())).thenReturn(Optional.of(claims));

            NewMemberResponse newMemberResponse = new NewMemberResponse();

            newMemberResponse.setId(claims.getSub());
            newMemberResponse.setRoomId(roomId);

            when(externalRoomService.addNewMember(eq(claims), eq(roomId), eq(headerAccessor.getSessionId())))
                    .thenReturn(newMemberResponse);

            assertTrue(simpWebSocketSessionManager.register(headerAccessor));

            Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();

            assertNotNull(sessionAttributes.get("user"));

            WebSocketSessionDTO sessionDTO = (WebSocketSessionDTO) sessionAttributes.get("user");

            assertEquals(claims.getUsername(), sessionDTO.getUsername());
            assertEquals(roomId, sessionDTO.getRoomId());
        }

    }
}
