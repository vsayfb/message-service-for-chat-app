package com.example.message_service.websocket;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketConfigIntTest {


    @LocalServerPort
    private int port;

    @Test
    void testConnection() throws ExecutionException, InterruptedException, UnknownHostException {
        StompSession session = connectWebsocket();

        assertTrue(session.isConnected());

        session.disconnect();

        assertFalse(session.isConnected());
    }

    @Test
    void testRelayBroker() throws ExecutionException, InterruptedException, UnknownHostException {
        StompSession session = connectWebsocket();

        assertTrue(session.isConnected());

        String message = "hello broker";

        CountDownLatch latch = new CountDownLatch(1);

        session.subscribe("/topic/room-1", new StompSessionHandlerAdapter() {

            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                String received = (String) payload;

                assertEquals(received, message);

                latch.countDown();
            }
        });

        session.send("/topic/room-1", message);

        boolean received = latch.await(2, TimeUnit.SECONDS);

        if (!received) {
            throw new RuntimeException("Message was not received. Broker may be not connected");
        }
    }

    StompSession connectWebsocket() throws ExecutionException, InterruptedException, UnknownHostException {

        String host = InetAddress.getLocalHost().getHostAddress();

        String wsEndpoint = "rooms";

        class StompSessionHandlerStub extends StompSessionHandlerAdapter {
        }

        WebSocketClient webSocketClient = new StandardWebSocketClient();

        WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);

        stompClient.setMessageConverter(new StringMessageConverter());

        String url = String.format("ws://%s:%s/%s", host, port, wsEndpoint);

        StompSessionHandler sessionHandler = new StompSessionHandlerStub();

        CompletableFuture<StompSession> asyncSession = stompClient.connectAsync(url, sessionHandler);

        return asyncSession.get();
    }
}
