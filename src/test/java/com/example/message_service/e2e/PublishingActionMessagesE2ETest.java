package com.example.message_service.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
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
import com.example.message_service.jwt.claims.JWTClaims;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class PublishingActionMessagesE2ETest {

    @Autowired
    private WebSocketHelper webSocketHelper;

    @Autowired
    private JWTHelper jwtHelper;

    @MockBean
    private ExternalRoomService externalRoomService;

    @LocalServerPort
    private int port;

    @Test
    void shouldJoinMessageBePublishedWhenNewMemberJoinsRoom()
            throws Exception {

        String roomId = UUID.randomUUID().toString();

        String destination = "/topic/" + roomId;

        webSocketHelper.setMessageConverter(new MappingJackson2MessageConverter());

        StompSession stompSession = webSocketHelper.connect("rooms", port);

        CountDownLatch latch = new CountDownLatch(99);

        stompSession.subscribe(destination, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return RoomMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {

                RoomMessage message = (RoomMessage) payload;

                assertEquals(message.getAction().getType(), RoomMessageAction.Type.JOIN);

                latch.countDown();
            }
        });

        AtomicInteger index = new AtomicInteger(0);

        NewMemberResponse[] members = addMembers(100, roomId);

        for (int i = 0; i < 100; i++) {

            when(externalRoomService.addNewMember(any(JWTClaims.class), eq(roomId)))
                    .thenAnswer(new Answer<NewMemberResponse>() {

                        @Override
                        public NewMemberResponse answer(InvocationOnMock invocation) throws Throwable {
                            return members[index.incrementAndGet()];
                        }
                    });

            subscribeRoom(members[i], destination);

        }

        boolean allPublished = latch.await(5, TimeUnit.SECONDS);

        assertTrue(latch.getCount() == 0);

        assertTrue(allPublished);
    }

    @Test
    void shouldLeaveMessageBePublishedWhenMemberLeavesRoom()
            throws Exception {

        String roomId = UUID.randomUUID().toString();

        String destination = "/topic/" + roomId;

        webSocketHelper.setMessageConverter(new MappingJackson2MessageConverter());

        AtomicInteger index = new AtomicInteger(0);

        NewMemberResponse[] members = addMembers(100, roomId);

        StompSession[] stompSessions = new StompSession[100];

        for (int i = 0; i < 100; i++) {

            when(externalRoomService.addNewMember(any(JWTClaims.class), eq(roomId)))
                    .thenAnswer(new Answer<NewMemberResponse>() {

                        @Override
                        public NewMemberResponse answer(InvocationOnMock invocation) throws Throwable {
                            return members[index.incrementAndGet()];
                        }
                    });

            stompSessions[i] = subscribeRoom(members[i], destination);
        }

        StompSession session = webSocketHelper.connect("rooms", port);

        CountDownLatch latch = new CountDownLatch(99);

        session.subscribe(destination, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return RoomMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {

                RoomMessage message = (RoomMessage) payload;

                assertEquals(message.getAction().getType(), RoomMessageAction.Type.LEAVE);

                latch.countDown();
            }
        });

        for (int i = 0; i < stompSessions.length; i++) {

            assertTrue(stompSessions[i].isConnected());

            stompSessions[i].disconnect();
        }

        boolean allPublished = latch.await(5, TimeUnit.SECONDS);

        assertTrue(latch.getCount() == 0);

        assertTrue(allPublished);
    }

    private StompSession subscribeRoom(NewMemberResponse member, String destination) throws Exception {

        jwtHelper.setUserId(member.getUserId());
        jwtHelper.setUsername(member.getUsername());

        String token = jwtHelper.sign();

        StompSession stompSession = webSocketHelper.connect("rooms", port);

        StompHeaders headers = new StompHeaders();

        headers.setDestination(destination);
        headers.add("Authorization", "Bearer " + token);

        stompSession.subscribe(headers, new StompFrameHandler() {

            @Override
            public Type getPayloadType(StompHeaders headers) {
                return null;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
            }
        });

        return stompSession;
    }

    private NewMemberResponse[] addMembers(int length, String roomId) {

        NewMemberResponse[] members = new NewMemberResponse[length];

        for (int i = 0; i < members.length; i++) {
            NewMemberResponse newMember = new NewMemberResponse();

            newMember.setUserId(UUID.randomUUID().toString());
            newMember.setUsername(generateRandomString());
            newMember.setRoomId(roomId);
            newMember.setMemberId(UUID.randomUUID().toString());
            newMember.setJoinedAt(new Date().toString());

            members[i] = newMember;
        }

        return members;
    }

    private String generateRandomString() {

        StringBuilder builder = new StringBuilder();

        Random random = new Random();

        for (int i = 0; i < 15; i++) {
            builder.append((char) (97 + random.nextInt(26)));
        }

        return builder.toString();
    }

}
