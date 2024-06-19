package com.example.message_service.external;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTest;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.message_service.external.dto.NewMemberResponse;
import com.example.message_service.jwt.claims.JWTClaims;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Nested;

@PactConsumerTest
public class ExternalRoomServiceContractTest {

    private ExternalRoomService externalRoomService;

    @Nested
    class AddMember {

        @Pact(consumer = "MessageService", provider = "RoomService")
        public RequestResponsePact nonExistentRoomPact(PactDslWithProvider builder) {

            return builder
                    .given("a non-existent room")
                    .uponReceiving("a request to create a member")
                    .path("/members/new/123")
                    .method("POST")
                    .willRespondWith()
                    .status(403)
                    .toPact();
        }

        @Test
        @PactTestFor(pactMethod = "nonExistentRoomPact", pactVersion = PactSpecVersion.V3)
        void nonExistentRoomPactTest(MockServer mockServer) {

            RestTemplate restTemplate = new RestTemplateBuilder().rootUri(mockServer.getUrl()).build();

            externalRoomService = new ExternalRoomService(restTemplate);

            externalRoomService.setRoomServiceUrl(mockServer.getUrl());

            JWTClaims claims = new JWTClaims();

            claims.setUserId("1234567");
            claims.setUsername("walter");

            String roomId = "123";

            assertThrows(RestClientException.class, () -> externalRoomService.addNewMember(claims, roomId));
        }

        @Pact(consumer = "MessageService", provider = "RoomService")
        public RequestResponsePact existentRoomPact(PactDslWithProvider builder) {

            return builder.given("an existent room")
                    .uponReceiving("a request to create new member")
                    .path("/members/new/345")
                    .method("POST")
                    .body(newJsonBody(json -> {
                        json.stringType("userId", "1234567");
                        json.stringType("username", "walter");
                    }).build())
                    .matchHeader("content-type", MediaType.APPLICATION_JSON_VALUE)
                    .willRespondWith()
                    .matchHeader("content-type", MediaType.APPLICATION_JSON_VALUE)
                    .status(201)
                    .body(newJsonBody(json -> {
                        json.stringType("memberId", "123456");
                        json.stringType("roomId", "345");
                        json.stringType("userId", "1234567");
                        json.stringType("username", "walter");
                        json.date("joinedAt", "yyyy-MM-dd'T'HH:mm:ssXXX");
                    }).build())
                    .toPact();
        }

        @Test
        @PactTestFor(pactMethod = "existentRoomPact", pactVersion = PactSpecVersion.V3)
        void existentRoomPactTest(MockServer mockServer) {

            RestTemplate restTemplate = new RestTemplateBuilder().rootUri(mockServer.getUrl()).build();

            externalRoomService = new ExternalRoomService(restTemplate);

            externalRoomService.setRoomServiceUrl(mockServer.getUrl());

            JWTClaims claims = new JWTClaims();

            claims.setUserId("1234567");
            claims.setUsername("walter");

            String roomId = "345";

            NewMemberResponse newMember = externalRoomService.addNewMember(claims, roomId);

            assertEquals(newMember.getMemberId(), "123456");
        }
    }

    @Nested
    class RemoveMember {

        @Pact(consumer = "MessageService", provider = "RoomService")
        public RequestResponsePact nonExistentMemberPact(PactDslWithProvider builder) {

            return builder.given("a non-existent")
                    .uponReceiving("a request to remove member")
                    .path("/members/123")
                    .method("DELETE")
                    .willRespondWith()
                    .status(403)
                    .toPact();

        }

        @Test
        @PactTestFor(pactMethod = "nonExistentMemberPact", pactVersion = PactSpecVersion.V3)
        void nonExistentMemberPactTest(MockServer mockServer) {

            RestTemplate restTemplate = new RestTemplateBuilder().rootUri(mockServer.getUrl()).build();

            externalRoomService = new ExternalRoomService(restTemplate);

            externalRoomService.setRoomServiceUrl(mockServer.getUrl());

            assertThrows(HttpClientErrorException.Forbidden.class, () -> externalRoomService.removeMember("123"));
        }

        @Pact(consumer = "MessageService", provider = "RoomService")
        public RequestResponsePact existentMemberPact(PactDslWithProvider builder) {

            return builder.given("an existent member")
                    .uponReceiving("a request to remove member")
                    .path("/members/345")
                    .method("DELETE")
                    .willRespondWith()
                    .status(200)
                    .toPact();

        }

        @Test
        @PactTestFor(pactMethod = "existentMemberPact", pactVersion = PactSpecVersion.V3)
        void existentMemberPactTest(MockServer mockServer) {

            RestTemplate restTemplate = new RestTemplateBuilder().rootUri(mockServer.getUrl()).build();

            externalRoomService = new ExternalRoomService(restTemplate);

            externalRoomService.setRoomServiceUrl(mockServer.getUrl());

            assertDoesNotThrow(() -> externalRoomService.removeMember("345"));
        }

    }
}
