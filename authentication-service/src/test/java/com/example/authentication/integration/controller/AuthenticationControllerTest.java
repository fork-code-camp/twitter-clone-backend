package com.example.authentication.integration.controller;

import com.example.authentication.entity.ActivationCode;
import com.example.authentication.integration.IntegrationTestBase;
import com.example.authentication.repository.ActivationCodeRepository;
import com.example.authentication.service.MessageSourceService;
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

    private static final String AUTH_REQ_PATTERN = "{\"email\": \"%s\", \"password\": \"test\"}";

    private static final String EXISTENT_ACCOUNT_EMAIL = "test@gmail.com";
    private static final String EXISTENT_ACCOUNT_JSON = AUTH_REQ_PATTERN.formatted(EXISTENT_ACCOUNT_EMAIL);

    private static final String NEW_ACCOUNT_EMAIL = "new_account@gmail.com";
    private static final String NEW_ACCOUNT_JSON = AUTH_REQ_PATTERN.formatted(NEW_ACCOUNT_EMAIL);

    private static final String REGISTER_URL = "/api/v1/auth/register";
    private static final String AUTHENTICATE_URL = "/api/v1/auth/authenticate";
    private static final String LOGOUT_URL = "/api/v1/auth/logout";
    private static final String ACTIVATION_URL = "/api/v1/auth/activate";
    private static final String TEST_URL = "/api/v1/test";

    private final MockMvc mockMvc;
    private final ActivationCodeRepository activationCodeRepository;
    private final MessageSourceService messageService;

    @Test
    void testRegisterSuccess() throws Exception {
        registerAccount(NEW_ACCOUNT_JSON);
        authenticateUnactivatedAccountAndExpectForbidden(NEW_ACCOUNT_JSON, NEW_ACCOUNT_EMAIL);
        activateAccount(NEW_ACCOUNT_EMAIL);
        String token = authenticateAccountAndExpectToken(NEW_ACCOUNT_JSON);
        testEndpointWithValidToken(token);
        logout(token);
        testEndpointWithInvalidToken(token);
    }

    @Test
    void testRegisterFailure() throws Exception {
        registerExistingAccountAndExpectFailure(EXISTENT_ACCOUNT_JSON, EXISTENT_ACCOUNT_EMAIL);
    }

    private void registerAccount(String account) throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                        .content(account)
                        .contentType(APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.message").value(messageService.generateMessage("activation.send.success"))
                );
    }

    private void registerExistingAccountAndExpectFailure(String account, String email) throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                        .content(account)
                        .contentType(APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.message").value(messageService.generateMessage("error.account.already_exists", email))
                );
    }

    private void activateAccount(String email) throws Exception {
        ActivationCode activationCode = activationCodeRepository.findActivationCodeByAccount_Email(email)
                .orElseThrow();

        mockMvc.perform(get(ACTIVATION_URL)
                        .param("activationCode", activationCode.getKey()))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.message").value(messageService.generateMessage("account.activation.success"))
                );
    }

    private String authenticateAccountAndExpectToken(String account) throws Exception {
        ResultActions result = mockMvc.perform(post(AUTHENTICATE_URL)
                        .content(account)
                        .contentType(APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.token").exists()
                );

        return extractTokenFromResponse(result);
    }

    private void authenticateUnactivatedAccountAndExpectForbidden(String account, String email) throws Exception {
        mockMvc.perform(post(AUTHENTICATE_URL)
                        .content(account)
                        .contentType(APPLICATION_JSON))
                .andExpectAll(
                        status().isForbidden(),
                        jsonPath("$.message").value(messageService.generateMessage("error.account.not_activated", email))
                );
    }

    private void testEndpointWithValidToken(String token) throws Exception {
        mockMvc.perform(get(TEST_URL)
                        .header("Authorization", "Bearer " + token))
                .andExpectAll(
                        status().isOk(),
                        content().string("Hello world!")
                );
    }

    private void testEndpointWithInvalidToken(String token) throws Exception {
        mockMvc.perform(get(TEST_URL)
                        .header("Authorization", "Bearer " + token))
                .andExpectAll(
                        status().isForbidden()
                );
    }

    private void logout(String token) throws Exception {
        mockMvc.perform(get(LOGOUT_URL)
                        .header("Authorization", "Bearer " + token))
                .andExpectAll(
                        status().isOk(),
                        content().string("")
                );
    }

    private String extractTokenFromResponse(ResultActions resultActions) throws Exception {
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
