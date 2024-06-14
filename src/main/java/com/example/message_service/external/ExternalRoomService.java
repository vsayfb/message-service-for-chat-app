package com.example.message_service.external;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class ExternalRoomService {

    private final RestTemplate restTemplate;

    public ExternalRoomService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public boolean checkRoomExists(String roomId) {

        try {
            ResponseEntity response = restTemplate.getForObject("http://room-service:8080/api/rooms/" + roomId, ResponseEntity.class);

            if (response == null) {
                return false;
            }

            return response.getStatusCode().equals(HttpStatus.OK);

        } catch (RestClientException e) {

            return false;
        }
    }
}