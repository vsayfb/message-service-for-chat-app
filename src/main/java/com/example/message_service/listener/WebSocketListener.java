package com.example.message_service.listener;

import java.util.Map;

import com.example.message_service.dto.WebSocketSessionDTO;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.example.message_service.dto.RoomMember;
import com.example.message_service.dto.RoomMessage;
import com.example.message_service.dto.RoomMessageAction;
import com.example.message_service.external.ExternalRoomService;
import com.example.message_service.external.dto.NewMemberResponse;
import com.example.message_service.publisher.RoomMessagePublisher;
import com.example.message_service.websocket.manager.WebSocketSessionManager;

@Component
public class WebSocketListener {

    private final RoomMessagePublisher roomMessagePublisher;
    private final WebSocketSessionManager webSocketSessionManager;

    public WebSocketListener(@Qualifier("simp") WebSocketSessionManager webSocketSessionManager,
            RoomMessagePublisher roomMessagePublisher) {
        this.webSocketSessionManager = webSocketSessionManager;
        this.roomMessagePublisher = roomMessagePublisher;
    }

    @EventListener
    void listenWebSocketDisconnection(SessionDisconnectEvent event) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        WebSocketSessionDTO websocketSession = webSocketSessionManager.getAuthenticatedUser(accessor);

        try {
            webSocketSessionManager.remove(accessor);

            RoomMessage roomMessage = new RoomMessage();

            roomMessage.setDestination("/topic/" + websocketSession.getRoomId());

            RoomMember roomMember = new RoomMember();

            roomMember.setMemberId(websocketSession.getMemberId());
            roomMember.setUserId(websocketSession.getUserId());
            roomMember.setUsername(websocketSession.getUsername());
            roomMember.setProfilePicture(websocketSession.getProfilePicture());

            RoomMessageAction<RoomMember> messageAction = new RoomMessageAction<>();

            messageAction.setType(RoomMessageAction.Type.LEAVE);
            messageAction.setSubject(roomMember);

            roomMessage.setAction(messageAction);

            roomMessagePublisher.publish(roomMessage);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
