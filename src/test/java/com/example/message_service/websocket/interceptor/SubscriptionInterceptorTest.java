package com.example.message_service.websocket.interceptor;

import com.example.message_service.websocket.manager.SimpWebSocketSessionManager;
import com.example.message_service.websocket.manager.WebSocketSessionManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionInterceptorTest {

    @Mock
    private WebSocketSessionManager webSocketSessionManager;

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
    void shouldReturnNullIfClientNotAuthenticated() {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);

        headerAccessor.setDestination("/topic/12312412");

        when(message.getHeaders()).thenReturn(headerAccessor.toMessageHeaders());

        when(webSocketSessionManager.register(any())).thenReturn(false);

        Message<?> result = subscriptionInterceptor.preSend(message, messageChannel);

        assertNull(result);
    }

    @Test
    void shouldReturnMessageIfClientAuthenticated() {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);

        headerAccessor.setDestination("/topic/12312412");

        when(message.getHeaders()).thenReturn(headerAccessor.toMessageHeaders());

        when(webSocketSessionManager.register(any())).thenReturn(true);

        Message<?> result = subscriptionInterceptor.preSend(message, messageChannel);

        assertNotNull(result);

    }

}
