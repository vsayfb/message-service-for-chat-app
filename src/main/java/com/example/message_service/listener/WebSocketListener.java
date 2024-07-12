package com.example.message_service.listener;

import com.example.message_service.dto.WebSocketSessionDTO;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.example.message_service.dto.RoomMember;
import com.example.message_service.dto.RoomMessage;
import com.example.message_service.dto.RoomMessageAction;
import com.example.message_service.publisher.RoomMessagePublisher;
import com.example.message_service.websocket.manager.WebSocketSessionManager;

@Component
public class WebSocketListener {

    private final RoomMessagePublisher roomMessagePublisher;
    private final WebSocketSessionManager webSocketSessionManager;

    public WebSocketListener(WebSocketSessionManager webSocketSessionManager,
            RoomMessagePublisher roomMessagePublisher) {
        this.webSocketSessionManager = webSocketSessionManager;
        this.roomMessagePublisher = roomMessagePublisher;
    }

    @EventListener
    void listenWebSocketDisconnection(SessionDisconnectEvent event) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        if (webSocketSessionManager.isAuthenticated(accessor)) {

            WebSocketSessionDTO authenticatedUser = webSocketSessionManager.getAuthenticatedUser(accessor);

            RoomMessage roomMessage = new RoomMessage();

            roomMessage.setDestination("/topic/" + authenticatedUser.getRoomId());

            RoomMember roomMember = new RoomMember();

            roomMember.setMemberId(authenticatedUser.getMemberId());
            roomMember.setUserId(authenticatedUser.getUserId());
            roomMember.setUsername(authenticatedUser.getUsername());
            roomMember.setProfilePicture(authenticatedUser.getProfilePicture());

            RoomMessageAction<RoomMember> messageAction = new RoomMessageAction<>();

            messageAction.setType(RoomMessageAction.Type.LEAVE);
            messageAction.setSubject(roomMember);

            roomMessage.setAction(messageAction);

            roomMessagePublisher.publish(roomMessage);

        }

        webSocketSessionManager.remove(accessor);

    }
}
