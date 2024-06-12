package com.example.message_service.websocket;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketConfigIntTest {


    @LocalServerPort
    private int port;

    @Test
    void testConnection() throws ExecutionException, InterruptedException, UnknownHostException {

        String host = InetAddress.getLocalHost().getHostAddress();

        String wsEndpoint = "rooms";

        class StompSessionHandlerStub extends StompSessionHandlerAdapter {
        }

        WebSocketClient webSocketClient = new StandardWebSocketClient();

        WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);

        String url =  String.format("ws://%s:%s/%s", host, port, wsEndpoint);

        StompSessionHandler sessionHandler = new StompSessionHandlerStub();

        CompletableFuture<StompSession> asyncSession = stompClient.connectAsync(url, sessionHandler);

        StompSession session = asyncSession.get();

        assertTrue(session.isConnected());

        session.disconnect();

        assertFalse(session.isConnected());
    }
}
