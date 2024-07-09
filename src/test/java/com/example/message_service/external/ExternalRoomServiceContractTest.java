package com.example.message_service.external;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTest;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.message_service.external.dto.NewMemberResponse;
import com.example.message_service.jwt.claims.JWTClaims;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonArrayMinLike;
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
                    .path("/members/new/58f50c7c-a58f-4bfc-a47b-af17f9dcac8c")
                    .matchHeader("content-type", MediaType.APPLICATION_JSON_VALUE)
                    .method("POST")
                    .body(newJsonBody(json -> {
                        json.stringType("userId");
                        json.stringType("username");
                        json.stringType("sessionId");
                        json.stringType("profilePicture");

                    }).build())
                    .matchHeader("content-type", MediaType.APPLICATION_JSON_VALUE)
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

            claims.setSub("1234567");
            claims.setUsername("walter");
            claims.setProfilePicture("http://");

            String roomId = "58f50c7c-a58f-4bfc-a47b-af17f9dcac8c";
            String sessionId = "4fab9115-6d6e-4e1b-8b3e-93078ed5cd47";

            assertThrows(RestClientException.class, () -> externalRoomService.addNewMember(claims, roomId, sessionId));
        }

        @Pact(consumer = "MessageService", provider = "RoomService")
        public RequestResponsePact existentRoomPact(PactDslWithProvider builder) {

            return builder.given("an existent room")
                    .uponReceiving("a request to create new member")
                    .path("/members/new/4fab9115-6d6e-4e1b-8b3e-93078ed5cd48")
                    .method("POST")
                    .body(newJsonBody(json -> {
                        json.stringType("userId");
                        json.stringType("username");
                        json.stringType("sessionId");
                        json.stringType("profilePicture");

                    }).build())
                    .matchHeader("content-type", MediaType.APPLICATION_JSON_VALUE)
                    .willRespondWith()
                    .matchHeader("content-type", MediaType.APPLICATION_JSON_VALUE)
                    .status(201)
                    .body(newJsonBody(json -> {
                        json.stringType("id");
                        json.stringType("roomId", "4fab9115-6d6e-4e1b-8b3e-93078ed5cd48");
                        newJsonArrayMinLike(1, array -> {
                            array.stringType("");
                        });
                        json.date("joinedAt", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
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

            claims.setSub("1234567");
            claims.setUsername("walter");
            claims.setProfilePicture("http://");

            String roomId = "4fab9115-6d6e-4e1b-8b3e-93078ed5cd48";
            String sessionId = "4fab9115-6d6e-4e1b-8b3e-93078ed5cd47";

            NewMemberResponse newMember = externalRoomService.addNewMember(claims, roomId, sessionId);

            assertEquals(newMember.getRoomId(), roomId);
        }
    }

    @Nested
    class RemoveMember {

        String memberId = "98696d01-77e0-4147-aac4-2629952742ec";
        String sessionId = "4fab9115-6d6e-4e1b-8b3e-93078ed5cd47";

        @Pact(consumer = "MessageService", provider = "RoomService")
        public RequestResponsePact existentMemberPact(PactDslWithProvider builder) {

            return builder.given("an existent member")
                    .uponReceiving("a request to remove member")
                    .path("/members/" + memberId + "/" + sessionId)
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

            assertDoesNotThrow(
                    () -> externalRoomService.removeMember(memberId, sessionId));
        }

    }
}
