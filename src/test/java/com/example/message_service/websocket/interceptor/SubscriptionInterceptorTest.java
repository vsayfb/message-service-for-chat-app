package com.example.message_service.websocket.interceptor;

import com.example.message_service.external.ExternalRoomService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ExecutorSubscribableChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class SubscriptionInterceptorTest {


    @Mock
    private ExternalRoomService externalRoomService;

    @Mock
    private Message<String> message;

    @Mock
    private MessageChannel messageChannel;

    @InjectMocks
    private SubscriptionInterceptor subscriptionInterceptor;



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
    void shouldReturnNullWhenDestRoomNotExist() {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);

        headerAccessor.setDestination("/topic/12312412");

        when(message.getHeaders()).thenReturn(headerAccessor.toMessageHeaders());

        when(externalRoomService.checkRoomExists(anyString())).thenReturn(false);

        Message<?> result = subscriptionInterceptor.preSend(message, messageChannel);

        assertNull(result);
    }

    @Test
    void shouldReturnMessageIfCommandNotSubscribe() {

        Message<?> result = subscriptionInterceptor.preSend(message, messageChannel);

        assertNotNull(result);
    }

    @Test
    void shouldReturnMessage() {

        GenericMessage<String> genericMessage = new GenericMessage<>("hello there");

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        headerAccessor.setDestination("/topic/12312412");

        Message<String> messageWithHeaders = MessageBuilder.createMessage(
                genericMessage.getPayload(),
                headerAccessor.getMessageHeaders()
        );

        when(externalRoomService.checkRoomExists("12312412")).thenReturn(true);

        Message<?> result = subscriptionInterceptor.preSend(messageWithHeaders, messageChannel);

        assertNotNull(result);
        assertEquals("hello there", result.getPayload());
    }
}
