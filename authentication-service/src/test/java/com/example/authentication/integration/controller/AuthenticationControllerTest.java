package com.example.authentication.integration.controller;

import com.example.authentication.entity.ActivationCode;
import com.example.authentication.integration.IntegrationTestBase;
import com.example.authentication.integration.mocks.ProfileMock;
import com.example.authentication.repository.ActivationCodeRepository;
import com.example.authentication.service.MessageSourceService;
import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static com.example.authentication.integration.constants.AuthConstants.EXISTENT_ACCOUNT_EMAIL;
import static com.example.authentication.integration.constants.AuthConstants.NEW_ACCOUNT_EMAIL;
import static com.example.authentication.integration.constants.JsonConstants.EXISTENT_ACCOUNT_JSON;
import static com.example.authentication.integration.constants.JsonConstants.NEW_ACCOUNT_JSON;
import static com.example.authentication.integration.constants.UrlConstants.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@RequiredArgsConstructor
public class AuthenticationControllerTest extends IntegrationTestBase {

    private final MockMvc mockMvc;
    private final ActivationCodeRepository activationCodeRepository;
    private final MessageSourceService messageService;
    private final WireMockServer mockProfileService;

    @BeforeEach
    void setUp() {
        ProfileMock.setupMockProfileResponse(mockProfileService);
    }

    @Test
    void testRegisterSuccess() throws Exception {
        registerAccount(NEW_ACCOUNT_JSON.getConstant());
        authenticateUnactivatedAccountAndExpectForbidden(NEW_ACCOUNT_JSON.getConstant(), NEW_ACCOUNT_EMAIL.getConstant());
        activateAccount(NEW_ACCOUNT_EMAIL.getConstant());
        String token = authenticateAccountAndExpectToken(NEW_ACCOUNT_JSON.getConstant());
        testEndpointWithValidToken(token);
        logout(token);
        testEndpointWithInvalidToken(token);
    }

    @Test
    void testRegisterFailure() throws Exception {
        registerExistingAccountAndExpectFailure(EXISTENT_ACCOUNT_JSON.getConstant(), EXISTENT_ACCOUNT_EMAIL.getConstant());
    }

    private void registerAccount(String account) throws Exception {
        mockMvc.perform(post(REGISTER_URL.getConstant())
                        .content(account)
                        .contentType(APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.message").value(messageService.generateMessage("activation.send.success"))
                );
    }

    private void registerExistingAccountAndExpectFailure(String account, String email) throws Exception {
        mockMvc.perform(post(REGISTER_URL.getConstant())
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

        mockMvc.perform(get(ACTIVATION_URL.getConstant())
                        .param("activationCode", activationCode.getKey()))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.message").value(messageService.generateMessage("account.activation.success"))
                );
    }

    private String authenticateAccountAndExpectToken(String account) throws Exception {
        ResultActions result = mockMvc.perform(post(AUTHENTICATE_URL.getConstant())
                        .content(account)
                        .contentType(APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.jwt").exists()
                );

        return extractTokenFromResponse(result);
    }

    private void authenticateUnactivatedAccountAndExpectForbidden(String account, String email) throws Exception {
        mockMvc.perform(post(AUTHENTICATE_URL.getConstant())
                        .content(account)
                        .contentType(APPLICATION_JSON))
                .andExpectAll(
                        status().isForbidden(),
                        jsonPath("$.message").value(messageService.generateMessage("error.account.not_activated", email))
                );
    }

    private void testEndpointWithValidToken(String token) throws Exception {
        mockMvc.perform(get(TEST_URL.getConstant())
                        .header("Authorization", "Bearer " + token))
                .andExpectAll(
                        status().isOk(),
                        content().string("Hello from secure endpoint!")
                );
    }

    private void testEndpointWithInvalidToken(String token) throws Exception {
        mockMvc.perform(get(TEST_URL.getConstant())
                        .header("Authorization", "Bearer " + token))
                .andExpectAll(
                        status().isForbidden()
                );
    }

    private void logout(String token) throws Exception {
        mockMvc.perform(get(LOGOUT_URL.getConstant())
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
                .at("/jwt")
                .asText();
    }
}
