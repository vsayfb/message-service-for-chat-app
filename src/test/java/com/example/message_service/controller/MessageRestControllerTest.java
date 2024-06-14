package com.example.message_service.controller;

import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class MessageRestControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    void shouldReturnBadRequest() throws Exception {

        mockMvc.perform(post("/messages/subscribe_writing/12345"))
                .andExpect(status().isBadRequest());

    }

    @Test
    void shouldReturnUnauthorized() throws Exception {

        mockMvc.perform(post("/messages/subscribe_writing/12345")
                        .header("x-jwt-userId", "")
                        .header("x-jwt-username", ""))
                .andExpect(status().isUnauthorized());

    }

    @Test
    void shouldReturnToken() throws Exception {

        mockMvc.perform(post("/messages/subscribe_writing/12345")
                        .header("x-jwt-userId", "123456")
                        .header("x-jwt-username", "username"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token", Is.is(Matchers.startsWith("ey"))));

    }
}

