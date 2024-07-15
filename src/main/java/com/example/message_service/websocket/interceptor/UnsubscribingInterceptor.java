package com.example.message_service.websocket.interceptor;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import com.example.message_service.dto.RoomMember;
import com.example.message_service.dto.RoomMessage;
import com.example.message_service.dto.RoomMessageAction;
import com.example.message_service.dto.WebSocketSessionDTO;
import com.example.message_service.publisher.RoomMessagePublisher;
import com.example.message_service.websocket.manager.WebSocketSessionManager;

@Component
public class UnsubscribingInterceptor implements ChannelInterceptor {

    private final WebSocketSessionManager webSocketSessionManager;
    private final RoomMessagePublisher roomMessagePublisher;

    public UnsubscribingInterceptor(WebSocketSessionManager webSocketSessionManager,
            RoomMessagePublisher roomMessagePublisher) {
        this.webSocketSessionManager = webSocketSessionManager;
        this.roomMessagePublisher = roomMessagePublisher;
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.UNSUBSCRIBE.equals(accessor.getCommand())) {

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
}
