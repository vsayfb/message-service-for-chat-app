package com.example.message_service.helper;

import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class WebSocketHelper {

    public StompSession connect(String endpoint, int port) throws ExecutionException, InterruptedException, UnknownHostException {

        String host = InetAddress.getLocalHost().getHostAddress();

        class StompSessionHandlerStub extends StompSessionHandlerAdapter {
        }

        WebSocketClient webSocketClient = new StandardWebSocketClient();

        WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);

        stompClient.setMessageConverter(new StringMessageConverter());

        String url = String.format("ws://%s:%s/%s", host, port, endpoint);

        StompSessionHandler sessionHandler = new StompSessionHandlerStub();

        CompletableFuture<StompSession> asyncSession = stompClient.connectAsync(url, sessionHandler);

        return asyncSession.get();
    }
}
