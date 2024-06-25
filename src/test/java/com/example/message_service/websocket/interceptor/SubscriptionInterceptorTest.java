package com.example.message_service.websocket.interceptor;

import com.example.message_service.dto.WebSocketSessionDTO;
import com.example.message_service.external.ExternalRoomService;
import com.example.message_service.external.dto.NewMemberResponse;
import com.example.message_service.jwt.JWTValidator;
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
import org.springframework.web.client.HttpClientErrorException;

import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionInterceptorTest {

    @Mock
    private ExternalRoomService externalRoomService;

    @Mock
    private JWTValidator jwtValidator;

    @Mock
    private Message<String> message;

    @Mock
    private MessageChannel messageChannel;

    @InjectMocks
    private SubscriptionInterceptor subscriptionInterceptor;

    @Test
    void shouldReturnMessageIfCommandNotSubscribe() {

        Message<?> result = subscriptionInterceptor.preSend(message, messageChannel);

        assertNotNull(result);
    }

    @Test
    void shouldReturnNullWhenDestEmpty() {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);

        headerAccessor.setDestination(null);

        when(message.getHeaders()).thenReturn(headerAccessor.toMessageHeaders());

        Message<?> result = subscriptionInterceptor.preSend(message, messageChannel);

        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenDestNotStartWithTopic() {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);

        headerAccessor.setDestination("/gopic");

        when(message.getHeaders()).thenReturn(headerAccessor.toMessageHeaders());

        Message<?> result = subscriptionInterceptor.preSend(message, messageChannel);

        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenDestRoomIdIsNull() {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);

        headerAccessor.setDestination("/topic/");

        when(message.getHeaders()).thenReturn(headerAccessor.toMessageHeaders());

        Message<?> result = subscriptionInterceptor.preSend(message, messageChannel);

        assertNull(result);
    }

    @Test
    void shouldReturnMessageIfClientIsNotAuthenticated() {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);

        headerAccessor.setDestination("/topic/12312412");
        headerAccessor.setNativeHeader("Authorization", null);

        when(message.getHeaders()).thenReturn(headerAccessor.toMessageHeaders());

        when(jwtValidator.validateToken(any())).thenReturn(Optional.empty());

        Message<?> result = subscriptionInterceptor.preSend(message, messageChannel);

        assertNotNull(result);

    }

    @Test
    void shouldReturnNullIfExternalErrorOccurs() {

        String roomId = "123456";

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);

        headerAccessor.setDestination("/topic/" + roomId);
        headerAccessor.setNativeHeader("Authorization", "ey");

        when(message.getHeaders()).thenReturn(headerAccessor.toMessageHeaders());

        JWTClaims claims = new JWTClaims();

        claims.setSub("123456");

        when(jwtValidator.validateToken("ey")).thenReturn(Optional.of(claims));

        when(externalRoomService.addNewMember(claims, roomId)).thenThrow(HttpClientErrorException.Forbidden.class);

        Message<?> result = subscriptionInterceptor.preSend(message, messageChannel);

        assertNull(result);
    }

    @Test
    void shouldPutSessionAttributesAndReturnMessage() {

        GenericMessage<String> genericMessage = new GenericMessage<>("");

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);

        String roomId = "123456";

        headerAccessor.setDestination("/topic/" + roomId);
        headerAccessor.setNativeHeader("Authorization", "ey");
        headerAccessor.setSessionAttributes(new HashMap<>());

        Message<String> messageWithHeaders = MessageBuilder.createMessage(
                genericMessage.getPayload(),
                headerAccessor.getMessageHeaders());

        JWTClaims claims = new JWTClaims();

        claims.setSub("1234");

        when(jwtValidator.validateToken("ey")).thenReturn(Optional.of(claims));

        NewMemberResponse memberResponse = new NewMemberResponse();

        memberResponse.setId("5678");

        when(externalRoomService.addNewMember(claims, roomId)).thenReturn(memberResponse);

        Message<?> result = subscriptionInterceptor.preSend(messageWithHeaders, messageChannel);

        assertNotNull(result);

        WebSocketSessionDTO accessor = (WebSocketSessionDTO) headerAccessor.getSessionAttributes().get("user");

        assertEquals(accessor.getMemberId(), memberResponse.getId());
        assertEquals(accessor.getUserId(), claims.getSub());
    }
}
