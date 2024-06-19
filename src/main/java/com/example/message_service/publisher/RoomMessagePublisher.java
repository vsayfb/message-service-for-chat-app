package com.example.message_service.publisher;

import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.example.message_service.dto.RoomMessage;

@Component
public class RoomMessagePublisher {

    private final SimpMessagingTemplate template;

    public RoomMessagePublisher(@Lazy SimpMessagingTemplate template) {
        this.template = template;
    }

    public void publish(RoomMessage message) {
        template.convertAndSend(message.getDestination(), message);
    }
}
