package com.example.authentication.integration.controller;

import com.example.authentication.integration.IntegrationTestBase;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@RequiredArgsConstructor
public class AuthControllerTest extends IntegrationTestBase {

    private static final String TEST_JSON = "{\"email\": \"test@gmail.com\", \"password\": \"test\"}";
    private static final String UNIQUE_JSON = "{\"email\": \"unique@gmail.com\", \"password\": \"test\"}";
    private final MockMvc mockMvc;

    @Test
    void registerSuccess() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .content(UNIQUE_JSON)
                        .contentType(APPLICATION_JSON)
                )
                .andExpectAll(
                        status().isOk(),
                        content().string("")
                );

        mockMvc.perform(post("/auth/login")
                        .content(UNIQUE_JSON)
                        .contentType(APPLICATION_JSON)
                )
                .andExpectAll(
                        status().isOk(),
                        content().string("you log in successfully")
                );
    }

    @Test
    void registerFail() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .content(TEST_JSON)
                        .contentType(APPLICATION_JSON)
                )
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.message").value("Account already exists: test@gmail.com")
                );
    }
}
