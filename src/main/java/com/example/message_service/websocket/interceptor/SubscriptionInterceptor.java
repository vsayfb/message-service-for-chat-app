package com.example.message_service.websocket.interceptor;

import com.example.message_service.dto.RoomMember;
import com.example.message_service.dto.RoomMessage;
import com.example.message_service.dto.RoomMessageAction;
import com.example.message_service.dto.WebSocketSessionDTO;
import com.example.message_service.publisher.RoomMessagePublisher;
import com.example.message_service.websocket.manager.WebSocketSessionManager;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionInterceptor implements ChannelInterceptor {

    private final WebSocketSessionManager webSocketSessionManager;
    private final RoomMessagePublisher roomMessagePublisher;

    public SubscriptionInterceptor(@Qualifier("simp") WebSocketSessionManager webSocketSessionManager,
            RoomMessagePublisher roomMessagePublisher) {
        this.webSocketSessionManager = webSocketSessionManager;
        this.roomMessagePublisher = roomMessagePublisher;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.SUBSCRIBE.equals(headerAccessor.getCommand())) {
            String dest = headerAccessor.getDestination();

            if (dest == null || !dest.startsWith("/topic/")) {
                return null;
            }

            String roomId = dest.substring(7);

            if (roomId.isEmpty()) {
                return null;
            }

            boolean registered = webSocketSessionManager.register(headerAccessor);

            return registered ? message : null;
        }

        return message;
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.SUBSCRIBE.equals(headerAccessor.getCommand())) {

            if (webSocketSessionManager.isGuest(headerAccessor)) {
                return;
            }

            try {
                WebSocketSessionDTO websocketSession = webSocketSessionManager.getAuthenticatedUser(headerAccessor);

                RoomMessage roomMessage = new RoomMessage();

                roomMessage.setDestination("/topic/" + websocketSession.getRoomId());

                RoomMember roomMember = new RoomMember();

                roomMember.setMemberId(websocketSession.getMemberId());
                roomMember.setUserId(websocketSession.getUserId());
                roomMember.setUsername(websocketSession.getUsername());
                roomMember.setProfilePicture(websocketSession.getProfilePicture());

                RoomMessageAction<RoomMember> messageAction = new RoomMessageAction<>();

                messageAction.setType(RoomMessageAction.Type.JOIN);
                messageAction.setSubject(roomMember);

                roomMessage.setAction(messageAction);

                roomMessagePublisher.publish(roomMessage);
            } catch (Exception e) {
                // TODO: handle exception
            }

        }
    }
}
