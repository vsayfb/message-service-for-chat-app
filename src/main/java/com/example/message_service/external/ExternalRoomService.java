package com.example.message_service.external;

import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Setter
public class ExternalRoomService {

    private String roomServiceUrl = "http://room-service:8080";

    private final RestTemplate restTemplate;

    public ExternalRoomService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean checkRoomExists(String roomId) {

        try {
            ResponseEntity<?> response = restTemplate.getForEntity(roomServiceUrl + "/api/rooms/" + roomId, Object.class);

            return response.getStatusCode().equals(HttpStatus.OK);

        } catch (RestClientException e) {

            return false;
        }
    }
}