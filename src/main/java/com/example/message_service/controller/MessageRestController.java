
package com.example.message_service.controller;

import com.example.message_service.jwt.JWTSigner;
import com.example.message_service.jwt.claims.JWTPayload;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RequestMapping("/messages")
@RestController
public class MessageRestController {

    private final JWTSigner jwtSigner;

    public MessageRestController(JWTSigner jwtSigner) {
        this.jwtSigner = jwtSigner;
    }

    @PostMapping("/subscribe_writing/{roomId}")
    ResponseEntity<?> subscribe(
            @PathVariable("roomId") String roomId,
            @RequestHeader("x-jwt-userId") String userId,
            @RequestHeader("x-jwt-username") String username) {

        if (userId.isEmpty() || username.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        JWTPayload jwtPayload = new JWTPayload();

        jwtPayload.setUsername(username);
        jwtPayload.setRoomId(roomId);
        jwtPayload.setUserId(userId);

        String token = jwtSigner.sign(jwtPayload);

        HashMap<String, HashMap<String, String>> response = new HashMap<>();

        HashMap<String, String> data = new HashMap<>();

        data.put("token", token);

        response.put("data", data);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}