package com.example.message_service.controller;

import com.example.message_service.dto.RoomMessage;
import com.example.message_service.external.ExternalRoomService;
import com.example.message_service.helper.WebSocketHelper;
import com.example.message_service.jwt.JWTSigner;
import com.example.message_service.jwt.claims.JWTPayload;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MessageMappingControllerE2ETest {

    @LocalServerPort
    private int port;

    @MockBean
    private ExternalRoomService externalRoomService;

    @Autowired
    private JWTSigner jwtSigner;

    @Autowired
    private WebSocketHelper webSocketHelper;

    @Test
    void shouldSubscribeAndSendMessages() throws UnknownHostException, ExecutionException, InterruptedException {
        // preventing SubscriptionInterceptor from making a real request to RoomService.
        when(externalRoomService.checkRoomExists(anyString())).thenReturn(true);

        String roomId = "123456";

        JWTPayload payload = new JWTPayload();

        payload.setUserId("987654");
        payload.setUsername("walter");
        payload.setRoomId(roomId);

        String token = jwtSigner.sign(payload);

        webSocketHelper.setMessageConverter(new MappingJackson2MessageConverter());

        StompSession session = webSocketHelper.connect("rooms", port);

        CountDownLatch latch = new CountDownLatch(5);

        var subscription = session.subscribe("/topic/" + roomId, new StompSessionHandlerAdapter() {

            @Override
            public Type getPayloadType(StompHeaders headers) {
                return RoomMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                RoomMessage roomMessage = (RoomMessage) payload;

                assertEquals("hello world", roomMessage.getContent());
                assertEquals("walter", roomMessage.getUsername());
                assertEquals(roomId, roomMessage.getRoomId());

                latch.countDown();
            }
        });

        System.out.println("subscription -> " + subscription);


        String clientMessage = "hello world";

        StompHeaders headers = new StompHeaders();

        headers.set("Authorization", "Bearer " + token);
        headers.setDestination("/messages/" + roomId);
        headers.setSubscription("sub-0");
        headers.setMessageId("pninbnlt-0");
        headers.setContentLength(clientMessage.length());


        session.send(headers, clientMessage);
        session.send(headers, clientMessage);
        session.send(headers, clientMessage);
        session.send(headers, clientMessage);
        session.send(headers, clientMessage);


        boolean received = latch.await(5, TimeUnit.SECONDS);

        assertTrue(received);

        // called in SubscriptionInterceptor to check if room exists
        verify(externalRoomService, times(1)).checkRoomExists(roomId);
    }
}
