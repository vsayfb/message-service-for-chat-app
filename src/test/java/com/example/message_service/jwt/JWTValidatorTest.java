package com.example.message_service.jwt;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.message_service.jwt.claims.JWTClaims;

@ExtendWith(MockitoExtension.class)
public class JWTValidatorTest {

    private JWTValidator jwtValidator;

    private String base64URLEncoded = "SEVMTE9USEVSRUhPV1lPVURPSU5HVE9EQVlJU0VWRVJZVEhJTkdPS0FZ";

    @BeforeEach
    void beforeEach() {
        jwtValidator = new JWTValidator(base64URLEncoded);

    }

    @Test
    void shouldHandleNullJWT() {
        Optional<JWTClaims> result = jwtValidator.validateToken(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldHandleNullEmptyJWT() {
        Optional<JWTClaims> result = jwtValidator.validateToken("");
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldHandleJWTNotStartsWithBearer() {
        Optional<JWTClaims> result = jwtValidator.validateToken("Baearer ");
        assertTrue(result.isEmpty());
    }

}
