package com.example.message_service.external;


import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExternalRoomServiceTest {


    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    ExternalRoomService externalRoomService;


    @Test
    void shouldReturnFalseIfStatusNotOK() {

        String roomId = "123123141";

        when(restTemplate.getForEntity("http://room-service:8080/api/rooms/" + roomId, Object.class))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        assertFalse(externalRoomService.checkRoomExists(roomId));
    }


    @Test
    void shouldReturnFalseIfRestThrowsError() {

        String roomId = "123123141";

        when(restTemplate.getForEntity("http://room-service:8080/api/rooms/" + roomId, Object.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertFalse(externalRoomService.checkRoomExists(roomId));
    }

    @Test
    void shouldReturnTrue() {

        String roomId = "123123141";

        when(restTemplate.getForEntity("http://room-service:8080/api/rooms/" + roomId, Object.class))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        assertTrue(externalRoomService.checkRoomExists(roomId));
    }


}
