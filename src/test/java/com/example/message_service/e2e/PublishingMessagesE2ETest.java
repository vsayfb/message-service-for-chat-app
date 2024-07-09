package com.example.message_service.e2e;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;

import com.example.message_service.dto.RoomMessage;
import com.example.message_service.dto.RoomMessageAction;
import com.example.message_service.external.ExternalRoomService;
import com.example.message_service.external.dto.NewMemberResponse;
import com.example.message_service.helper.JWTHelper;
import com.example.message_service.helper.WebSocketHelper;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class PublishingMessagesE2ETest {

    @Autowired
    private WebSocketHelper webSocketHelper;

    @Autowired
    private JWTHelper jwtHelper;

    @MockBean
    private ExternalRoomService externalRoomService;

    @LocalServerPort
    private int port;

    @Test
    void shouldNotBeAbleToSendMessagesIfClientNonAMember()
            throws UnknownHostException, ExecutionException, InterruptedException {

        String roomId = UUID.randomUUID().toString();

        String destination = "/topic/" + roomId;

        webSocketHelper.setMessageConverter(new MappingJackson2MessageConverter());

        StompSession webSocketSession = webSocketHelper.connect("rooms", port);

        StompHeaders subscribeHeaders = new StompHeaders();

        subscribeHeaders.setDestination(destination);

        CountDownLatch latch = new CountDownLatch(1);

        webSocketSession.subscribe(subscribeHeaders, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return RoomMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                latch.countDown();
            }
        });

        StompHeaders messageHeaders = new StompHeaders();

        messageHeaders.setDestination("/messages/" + roomId);

        webSocketSession.send(messageHeaders, "Hello!");

        boolean sent = latch.await(5, TimeUnit.SECONDS);

        assertFalse(sent);
    }

    @Test
    void shouldBeAbleToSendMessagesIfClientMember()
            throws UnknownHostException, ExecutionException, InterruptedException {

        String roomId = UUID.randomUUID().toString();
        String clientId = UUID.randomUUID().toString();
        String clientUsername = "jessy";

        jwtHelper.setUserId(clientId);
        jwtHelper.setUsername(clientUsername);

        String token = jwtHelper.sign();

        NewMemberResponse newMember = new NewMemberResponse();

        newMember.setId(UUID.randomUUID().toString());
        newMember.setRoomId(roomId);
        newMember.setJoinedAt(new Date().toString());

        when(externalRoomService.addNewMember(any(), any(), any())).thenReturn(newMember);

        String destination = "/topic/" + roomId;

        webSocketHelper.setMessageConverter(new MappingJackson2MessageConverter());

        StompSession webSocketSession = webSocketHelper.connect("rooms", port);

        StompHeaders subscribeHeaders = new StompHeaders();

        subscribeHeaders.setDestination(destination);
        subscribeHeaders.add("Authorization", "Bearer " + token);

        CountDownLatch latch = new CountDownLatch(100);

        webSocketSession.subscribe(subscribeHeaders, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return RoomMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {

                RoomMessage message = (RoomMessage) payload;

                assertTrue(message.getAction().getType().equals(RoomMessageAction.Type.STANDARD));

                latch.countDown();
            }
        });

        StompHeaders messageHeaders = new StompHeaders();

        messageHeaders.setDestination("/messages/" + roomId);

        for (int i = 0; i < 100; i++) {
            webSocketSession.send(messageHeaders, "Mr White!");
        }

        boolean sent = latch.await(10, TimeUnit.SECONDS);

        assertTrue(sent);
    }
}
