package com.example.message_service.helper;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.example.message_service.external.dto.NewMemberResponse;

import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Setter
@Component
public class WebSocketHelper {

    @Autowired
    private JWTHelper jwtHelper;

    private MessageConverter messageConverter = new StringMessageConverter();

    public StompSession connect(String endpoint, int port)
            throws ExecutionException, InterruptedException, UnknownHostException {

        String host = InetAddress.getLocalHost().getHostAddress();

        class StompSessionHandlerStub extends StompSessionHandlerAdapter {
        }

        WebSocketClient webSocketClient = new StandardWebSocketClient();

        WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);

        stompClient.setMessageConverter(messageConverter);

        String url = String.format("ws://%s:%s/%s", host, port, endpoint);

        StompSessionHandler sessionHandler = new StompSessionHandlerStub();

        CompletableFuture<StompSession> asyncSession = stompClient.connectAsync(url, sessionHandler);

        return asyncSession.get();
    }

    public StompSession subscribeRoom(NewMemberResponse member, String endpoint, String destination, int port)
            throws UnknownHostException, ExecutionException, InterruptedException {

        jwtHelper.setUserId(member.getId());

        String token = jwtHelper.sign();

        StompSession stompSession = connect(endpoint, port);

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

}