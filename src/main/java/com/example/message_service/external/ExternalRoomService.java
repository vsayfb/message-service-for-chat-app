package com.example.message_service.external;

import lombok.Setter;

import java.util.HashMap;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.message_service.external.dto.NewMemberResponse;
import com.example.message_service.jwt.claims.JWTClaims;

@Service
@Setter
public class ExternalRoomService {

    private String roomServiceUrl = "http://room-service:8080";

    private final RestTemplate restTemplate;

    public ExternalRoomService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public NewMemberResponse addNewMember(JWTClaims claims, String roomId, String sessionId)
            throws RestClientException {

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);

        HashMap<String, String> body = new HashMap<>();

        body.put("username", claims.getUsername());
        body.put("userId", claims.getSub());
        body.put("sessionId", sessionId);
        body.put("profilePicture", claims.getProfilePicture());

        HttpEntity<HashMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<NewMemberResponse> response = restTemplate
                .postForEntity(roomServiceUrl + "/members/room/" + roomId, requestEntity,
                        NewMemberResponse.class);

        return response.getBody();
    }

    public void removeMember(String memberId, String sessionId) throws RestClientException {
        restTemplate.delete(roomServiceUrl + "/members/" + memberId + "/session/" + sessionId);
    }

    public void removeMemberByUserId(String userId, String roomId, String sessionId) throws RestClientException {
        restTemplate
                .delete(roomServiceUrl + "/members/user/" + userId + "/room/" + roomId + "/session/" + sessionId);
    }

    public NewMemberResponse getMemberByUserIdAndRoomId(String userId, String roomId)
            throws RestClientException {

        ResponseEntity<NewMemberResponse> response = restTemplate
                .getForEntity(roomServiceUrl + "/members/user/" + userId + "/room/" + roomId, NewMemberResponse.class);

        return response.getBody();
    }
}