package com.example.message_service.websocket.interceptor;

import com.example.message_service.dto.RoomMember;
import com.example.message_service.dto.RoomMessage;
import com.example.message_service.dto.RoomMessageAction;
import com.example.message_service.external.ExternalRoomService;
import com.example.message_service.external.dto.NewMemberResponse;
import com.example.message_service.jwt.JWTValidator;
import com.example.message_service.jwt.claims.JWTClaims;
import com.example.message_service.publisher.RoomMessagePublisher;

import java.util.Map;
import java.util.Optional;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class SubscriptionInterceptor implements ChannelInterceptor {

    private final ExternalRoomService externalRoomService;

    private final RoomMessagePublisher roomMessagePublisher;

    private JWTValidator jwtValidator;

    public SubscriptionInterceptor(ExternalRoomService externalRoomService, JWTValidator jwtValidator,
            RoomMessagePublisher roomMessagePublisher) {
        this.externalRoomService = externalRoomService;
        this.jwtValidator = jwtValidator;
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

            String token = headerAccessor.getFirstNativeHeader("Authorization");

            Optional<JWTClaims> optionalJWT = jwtValidator.validateToken(token);

            if (optionalJWT.isEmpty()) {
                return message;
            }

            try {
                NewMemberResponse newMember = externalRoomService.addNewMember(optionalJWT.get(), roomId);

                headerAccessor.getSessionAttributes().put("user", newMember);

            } catch (RestClientException e) {
                return null;
            }

        }

        return message;
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.SUBSCRIBE.equals(headerAccessor.getCommand())) {

            Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();

            if (sessionAttributes.isEmpty()) {
                return;
            }

            try {
                NewMemberResponse websocketSession = (NewMemberResponse) sessionAttributes.get("user");

                RoomMessage roomMessage = new RoomMessage();

                roomMessage.setDestination("/topics/" + websocketSession.getRoomId());

                RoomMember roomMember = new RoomMember();

                roomMember.setMemberId(websocketSession.getMemberId());
                roomMember.setUserId(websocketSession.getUserId());
                roomMember.setUsername(websocketSession.getUsername());

                RoomMessageAction<RoomMember> messageAction = new RoomMessageAction<>();

                messageAction.setType(RoomMessageAction.Type.JOIN);
                messageAction.setSubject(roomMember);

                roomMessagePublisher.publish(roomMessage);
            } catch (Exception e) {
                // TODO: handle exception
            }

        }
    }
}
