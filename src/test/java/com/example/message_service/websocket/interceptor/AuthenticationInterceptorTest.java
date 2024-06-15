package com.example.message_service.websocket.interceptor;

import com.example.message_service.jwt.JWTSigner;
import com.example.message_service.jwt.claims.JWTClaims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationInterceptorTest {

    @Mock
    private Message<String> message;

    @Mock
    private MessageChannel messageChannel;

    @Mock
    private JWTSigner jwtSigner;

    @InjectMocks
    private AuthenticationInterceptor authenticationInterceptor;


    @Test
    void shouldReturnNullWhenDestEmpty() {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SEND);

        headerAccessor.setDestination(null);

        when(message.getHeaders()).thenReturn(headerAccessor.toMessageHeaders());

        Message<?> result = authenticationInterceptor.preSend(message, messageChannel);

        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenDestNotStartWithMessages() {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SEND);

        headerAccessor.setDestination("/topic/");

        when(message.getHeaders()).thenReturn(headerAccessor.toMessageHeaders());

        Message<?> result = authenticationInterceptor.preSend(message, messageChannel);

        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenTokenNotFound() {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SEND);

        headerAccessor.setDestination("/messages/12345");

        when(message.getHeaders()).thenReturn(headerAccessor.toMessageHeaders());

        Message<?> result = authenticationInterceptor.preSend(message, messageChannel);

        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenTokenNotStartsWithBearer() {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SEND);

        headerAccessor.setDestination("/messages/12345");
        headerAccessor.setNativeHeader("Authorization", "token");

        when(message.getHeaders()).thenReturn(headerAccessor.toMessageHeaders());

        Message<?> result = authenticationInterceptor.preSend(message, messageChannel);

        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenTokenIsInvalid() {

        String token = "ey123daascascaswrqw";

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SEND);

        headerAccessor.setDestination("/messages/12312412");
        headerAccessor.setNativeHeader("Authorization", "Bearer " + token);

        when(message.getHeaders()).thenReturn(headerAccessor.toMessageHeaders());

        when(jwtSigner.validateToken(token)).thenReturn(Optional.empty());

        Message<?> result = authenticationInterceptor.preSend(message, messageChannel);

        assertNull(result);

        verify(jwtSigner, times(1)).validateToken(token);
    }

    @Test
    void shouldReturnNullIfRoomIdsNotMatched() {

        String token = "ey123daascascaswrqw";

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SEND);

        headerAccessor.setDestination("/messages/12345");
        headerAccessor.setNativeHeader("Authorization", "Bearer " + token);

        when(message.getHeaders()).thenReturn(headerAccessor.toMessageHeaders());

        JWTClaims jwtClaims = new JWTClaims();

        jwtClaims.setRoomId("123456");

        when(jwtSigner.validateToken(token)).thenReturn(Optional.of(jwtClaims));

        Message<?> result = authenticationInterceptor.preSend(message, messageChannel);

        assertNull(result);

        verify(jwtSigner, times(1)).validateToken(token);
    }

    @Test
    void shouldReturnMessage() {

        String token = "ey123daascascaswrqw";
        String roomId = "123456";
        String payload = "hello there";

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SEND);

        headerAccessor.setDestination("/messages/" + roomId);
        headerAccessor.setNativeHeader("Authorization", "Bearer " + token);

        JWTClaims jwtClaims = new JWTClaims();

        jwtClaims.setRoomId(roomId);

        when(jwtSigner.validateToken(token)).thenReturn(Optional.of(jwtClaims));

        GenericMessage<String> genericMessage = new GenericMessage<>(payload);

        Message<String> messageWithHeaders = MessageBuilder.createMessage(
                genericMessage.getPayload(),
                headerAccessor.getMessageHeaders()
        );

        Message<?> result = authenticationInterceptor.preSend(messageWithHeaders, messageChannel);

        assertNotNull(result);
        assertEquals("hello there", result.getPayload());

        System.out.println("result -> " + result);

        verify(jwtSigner, times(1)).validateToken(token);
    }
}
