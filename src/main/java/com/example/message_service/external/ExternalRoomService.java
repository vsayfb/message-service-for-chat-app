package com.example.message_service.external;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class ExternalRoomService {


    public boolean checkRoomExists(String roomId) {

        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity response = restTemplate.getForObject("http://room-service:8080/api/rooms/" + roomId, ResponseEntity.class);

            if(response.getStatusCode().equals(HttpStatus.OK)) {
                return true;
            }

        } catch (RestClientException e) {
            return false;
        }

        return false;
    }
}