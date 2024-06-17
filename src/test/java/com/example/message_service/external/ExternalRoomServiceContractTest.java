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
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

@PactConsumerTest
public class ExternalRoomServiceContractTest {

    private ExternalRoomService externalRoomService;

    @Pact(consumer = "MessageService", provider = "RoomService")
    public RequestResponsePact existingRoomPact(PactDslWithProvider builder) {

        return builder
                .given("an existing room")
                .uponReceiving("a request to find the existing room")
                .path("/rooms/123")
                .method("GET")
                .willRespondWith()
                .status(200)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "existingRoomPact", pactVersion = PactSpecVersion.V3)
    void existingRoomPactTest(MockServer mockServer) {

        RestTemplate restTemplate = new RestTemplateBuilder().rootUri(mockServer.getUrl()).build();

        externalRoomService = new ExternalRoomService(restTemplate);

        externalRoomService.setRoomServiceUrl(mockServer.getUrl());

        assertTrue(externalRoomService.checkRoomExists("123"));
    }

    @Pact(consumer = "MessageService", provider = "RoomService")
    public RequestResponsePact nonExistentRoomPact(PactDslWithProvider builder) {

        return builder.given("a room that does not exist")
                .uponReceiving("a request to find a non-existent room")
                .path("/rooms/345")
                .method("GET")
                .willRespondWith()
                .status(404)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "nonExistentRoomPact", pactVersion = PactSpecVersion.V3)
    void nonExistentRoomPactTest(MockServer mockServer) {

        RestTemplate restTemplate = new RestTemplateBuilder().rootUri(mockServer.getUrl()).build();

        externalRoomService = new ExternalRoomService(restTemplate);

        externalRoomService.setRoomServiceUrl(mockServer.getUrl());

        assertFalse(externalRoomService.checkRoomExists("345"));
    }

}
