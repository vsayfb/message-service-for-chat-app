package com.example.message_service.websocket;

import com.example.message_service.helper.WebSocketHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.simp.stomp.StompSession;

import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketConfigIntTest {

    @LocalServerPort
    private int port;

    @Autowired
    private WebSocketHelper webSocketHelper;

    @Test
    void testConnection() throws ExecutionException, InterruptedException, UnknownHostException {
        StompSession session = webSocketHelper.connect("rooms", port);

        assertTrue(session.isConnected());

        session.disconnect();

        assertFalse(session.isConnected());
    }
}
