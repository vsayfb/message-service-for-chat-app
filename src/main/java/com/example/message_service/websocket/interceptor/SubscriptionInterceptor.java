package com.example.message_service.websocket.interceptor;

import com.example.message_service.external.ExternalRoomService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionInterceptor implements ChannelInterceptor {

    private  final ExternalRoomService externalRoomService;

    public SubscriptionInterceptor(ExternalRoomService externalRoomService) {
        this.externalRoomService = externalRoomService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.SUBSCRIBE.equals(headerAccessor.getCommand())) {
            String dest = headerAccessor.getDestination();

            if (dest == null || !dest.startsWith("/topic/")) {
                return null;
            }

            String roomId = dest.substring("/topic/".length());

            if (roomId.isEmpty()) {
                return null;
            }

            if(!externalRoomService.checkRoomExists(roomId)){
                return null;
            }
        }

        return message;
    }
}
