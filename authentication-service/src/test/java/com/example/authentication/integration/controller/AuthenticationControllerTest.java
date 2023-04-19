package com.example.authentication.integration.controller;

import com.example.authentication.entity.ActivationCode;
import com.example.authentication.integration.IntegrationTestBase;
import com.example.authentication.repository.ActivationCodeRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@RequiredArgsConstructor
public class AuthenticationControllerTest extends IntegrationTestBase {

    private static final String EXISTING_ACC = "{\"email\": \"test@gmail.com\", \"password\": \"test\"}";
    private static final String UNIQUE_ACC = "{\"email\": \"unique@gmail.com\", \"password\": \"test\"}";

    private static final String REGISTER_URL = "/api/v1/auth/register";
    private static final String AUTHENTICATE_URL = "/api/v1/auth/authenticate";
    private static final String LOGOUT_URL = "/api/v1/auth/logout";
    private static final String ACTIVATION_URL = "/api/v1/auth/activate";
    private static final String TEST_URL = "/api/v1/test";

    private final MockMvc mockMvc;
    private final ActivationCodeRepository activationCodeRepository;

    @Test
    void registerSuccess() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                        .content(UNIQUE_ACC)
                        .contentType(APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.message").exists(),
                        jsonPath("$.message").isString(),
                        jsonPath("$.message").value("Activation code's been sent to your email!")
                );

        mockMvc.perform(post(AUTHENTICATE_URL)
                        .content(UNIQUE_ACC)
                        .contentType(APPLICATION_JSON))
                .andExpectAll(
                        status().isForbidden(),
                        jsonPath("$.message").exists(),
                        jsonPath("$.message").isString(),
                        jsonPath("$.message").value("Account not activated!")
                );

        // avoid: duplicate key value violates unique constraint
        Thread.sleep(1500L);

        ActivationCode activationCode = activationCodeRepository.findActivationCodeByAccount_Email("unique@gmail.com").orElseThrow();

        mockMvc.perform(get(ACTIVATION_URL)
                        .param("activationCode", activationCode.getKey()))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.message").exists(),
                        jsonPath("$.message").isString(),
                        jsonPath("$.message").value("Account successfully activated!")
                );

        ResultActions result = mockMvc.perform(post(AUTHENTICATE_URL)
                        .content(UNIQUE_ACC)
                        .contentType(APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.token").exists(),
                        jsonPath("$.token").isString()
                );

        mockMvc.perform(get(TEST_URL)
                        .header("Authorization", "Bearer " + getToken(result)))
                .andExpectAll(
                        status().isOk(),
                        content().string("Hello world!")
                );
    }

    @Test
    void registerFail() throws Exception {
//        mockMvc.perform(post(AUTHENTICATE_URL) TODO
//                        .content(EXISTING_ACC)
//                        .contentType(APPLICATION_JSON))
//                .andExpectAll(
//                        status().isOk(),
//                        jsonPath("$.token").exists(),
//                        jsonPath("$.token").isString()
//                );

        mockMvc.perform(post(REGISTER_URL)
                        .content(EXISTING_ACC)
                        .contentType(APPLICATION_JSON))
                .andExpectAll(
                        status().is4xxClientError(),
                        jsonPath("$.message").value("Account already exists: test@gmail.com")
                );


        mockMvc.perform(get(TEST_URL)
                        .header("Authorization", "dummy token"))
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    void logoutTest() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                        .content(UNIQUE_ACC)
                        .contentType(APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.message").exists(),
                        jsonPath("$.message").isString(),
                        jsonPath("$.message").value("Activation code's been sent to your email!")
                );

        ActivationCode activationCode = activationCodeRepository.findActivationCodeByAccount_Email("unique@gmail.com").orElseThrow();

        mockMvc.perform(get(ACTIVATION_URL)
                        .param("activationCode", activationCode.getKey()))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.message").exists(),
                        jsonPath("$.message").isString(),
                        jsonPath("$.message").value("Account successfully activated!")
                );

        ResultActions result = mockMvc.perform(post(AUTHENTICATE_URL)
                        .content(UNIQUE_ACC)
                        .contentType(APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.token").exists(),
                        jsonPath("$.token").isString()
                );

        String token = getToken(result);

        mockMvc.perform(get(TEST_URL)
                        .header("Authorization", "Bearer " + token))
                .andExpectAll(
                        status().isOk(),
                        content().string("Hello world!")
                );

        mockMvc.perform(get(LOGOUT_URL)
                        .header("Authorization", "Bearer " + token))
                .andExpectAll(
                        status().isOk(),
                        content().string("")
                );

        mockMvc.perform(get(TEST_URL)
                        .header("Authorization", "Bearer " + token))
                .andExpectAll(
                        status().isForbidden()
                );
    }

    private String getToken(ResultActions resultActions) throws Exception {
        return new ObjectMapper()
                .readTree(
                        resultActions.andReturn()
                                .getResponse()
                                .getContentAsString()
                )
                .at("/token")
                .asText();
    }
}
