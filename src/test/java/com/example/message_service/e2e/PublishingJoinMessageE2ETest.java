package com.example.message_service.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import com.example.message_service.helper.WebSocketHelper;
import com.example.message_service.jwt.claims.JWTClaims;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class PublishingJoinMessageE2ETest {

    @Autowired
    private WebSocketHelper webSocketHelper;

    @MockBean
    private ExternalRoomService externalRoomService;

    @LocalServerPort
    private int port;

    @Test
    void test()
            throws Exception {

        String roomId = UUID.randomUUID().toString();

        String destination = "/topic/" + roomId;

        webSocketHelper.setMessageConverter(new MappingJackson2MessageConverter());

        StompSession stompSession = webSocketHelper.connect("rooms", port);

        CountDownLatch latch = new CountDownLatch(9);

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

        NewMemberResponse[] members = addMembers(10, roomId);

        for (int i = 0; i < members.length - 1; i++) {

            when(externalRoomService.addNewMember(any(JWTClaims.class), eq(roomId), anyString()))
                    .thenAnswer(new Answer<NewMemberResponse>() {

                        @Override
                        public NewMemberResponse answer(InvocationOnMock invocation) throws Throwable {
                            return members[index.incrementAndGet()];
                        }
                    });

            webSocketHelper.subscribeRoom(members[i], "rooms", destination, port);

        }

        boolean allPublished = latch.await(5, TimeUnit.SECONDS);

        assertTrue(latch.getCount() == 0);

        assertTrue(allPublished);
    }

    private NewMemberResponse[] addMembers(int length, String roomId) {

        NewMemberResponse[] members = new NewMemberResponse[length];

        for (int i = 0; i < members.length; i++) {
            NewMemberResponse newMember = new NewMemberResponse();

            newMember.setId(UUID.randomUUID().toString());
            newMember.setRoomId(roomId);
            newMember.setJoinedAt(new Date().toString());

            members[i] = newMember;
        }

        return members;
    }

}
