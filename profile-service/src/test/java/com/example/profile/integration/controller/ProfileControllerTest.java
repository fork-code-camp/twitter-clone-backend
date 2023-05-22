package com.example.profile.integration.controller;

import com.example.profile.integration.IntegrationTestBase;
import com.example.profile.service.MessageSourceService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static com.example.profile.integration.constants.JsonConstants.*;
import static com.example.profile.integration.constants.ProfileConstants.*;
import static com.example.profile.integration.constants.UrlConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@RequiredArgsConstructor
public class ProfileControllerTest extends IntegrationTestBase {

    private final MockMvc mockMvc;
    private final MessageSourceService messageService;

    @Test
    public void createProfileTestSuccess() throws Exception {
        String profileId = createProfileSuccess(NEW_PROFILE_JSON.getConstant());
        String email = getProfileByIdSuccess(profileId, "/email");
        assertThat(email).isEqualTo(NEW_PROFILE_EMAIL.getConstant());
        getProfileByEmailSuccess(NEW_PROFILE_EMAIL.getConstant());
    }

    @Test
    public void createProfileTestFailure() throws Exception {
        createProfileSuccess(EXISTENT_PROFILE_JSON.getConstant());
        createProfileFailure(EXISTENT_PROFILE_JSON.getConstant());
        getProfileByIdFailure("dummy id");
        getProfileByEmailFailure("dummy email");
    }

    @Test
    public void updateProfileTestFailure() throws Exception {
        updateProfileNotFound("dummy profile id", "new username", UPDATE_PROFILE_EMAIL.getConstant());
    }

    @Test
    public void updateProfileTestSuccess() throws Exception {
        String profileId = createProfileSuccess(UPDATE_PROFILE_JSON.getConstant());
        updateProfileSuccess(profileId, "new username", UPDATE_PROFILE_EMAIL.getConstant());
        String username = getProfileByIdSuccess(profileId, "/username");
        assertThat(username).isEqualTo("new username");
        updateProfileForbidden(profileId, "new username");
    }

    private String createProfileSuccess(String content) throws Exception {
        return mockMvc.perform(post(PROFILE_URL.getConstant())
                        .content(content)
                        .contentType(APPLICATION_JSON))
                .andExpectAll(
                        status().isCreated(),
                        content().string(not(emptyString())),
                        content().string(hasLength(24)) // for example: 64638d5eb8f40048bde121d3
                )
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private void createProfileFailure(String content) throws Exception {
        mockMvc.perform(post(PROFILE_URL.getConstant())
                        .content(content)
                        .contentType(APPLICATION_JSON))
                .andExpectAll(
                        status().is4xxClientError(),
                        jsonPath("$.message").value(containsString("duplicate key error collection"))
                );
    }

    private String getProfileByIdSuccess(String profileId, String fieldToExtract) throws Exception {
        ResultActions resultActions = mockMvc.perform(get(PROFILE_BY_ID_URL.getConstant().formatted(profileId)))
                .andExpectAll(
                        status().is2xxSuccessful(),
                        content().string(not(emptyString()))
                );

        return extractFieldFromResponse(resultActions, fieldToExtract);
    }

    private void getProfileByIdFailure(String profileId) throws Exception {
        mockMvc.perform(get(PROFILE_BY_ID_URL.getConstant().formatted(profileId)))
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.message").value(messageService.generateMessage("error.entity.not_found", profileId))
                );
    }

    private void getProfileByEmailSuccess(String email) throws Exception {
        mockMvc.perform(get(PROFILE_ID_BY_EMAIL_URL.getConstant().formatted(email)))
                .andExpectAll(
                        status().is2xxSuccessful(),
                        content().string(not(emptyString())),
                        content().string(hasLength(24))
                );
    }

    private void getProfileByEmailFailure(String email) throws Exception {
        mockMvc.perform(get(PROFILE_ID_BY_EMAIL_URL.getConstant().formatted(email)))
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.message").value(messageService.generateMessage("error.entity.not_found", email))
                );
    }

    public void updateProfileSuccess(String profileId, String updateUsername, String authEmail) throws Exception {
        mockMvc.perform(patch(PROFILE_BY_ID_URL.getConstant().formatted(profileId))
                        .content(PROFILE_UPDATE_REQ_PATTERN.getConstant().formatted(updateUsername))
                        .contentType(APPLICATION_JSON)
                        .header("loggedInUser", authEmail))
                .andExpectAll(
                        status().is2xxSuccessful(),
                        jsonPath("$.email").value(authEmail),
                        jsonPath("$.username").value(updateUsername)
                );
    }

    public void updateProfileNotFound(String profileId, String updateUsername, String authEmail) throws Exception {
        mockMvc.perform(patch(PROFILE_BY_ID_URL.getConstant().formatted(profileId))
                        .content(PROFILE_UPDATE_REQ_PATTERN.getConstant().formatted(updateUsername))
                        .contentType(APPLICATION_JSON)
                        .header("loggedInUser", authEmail))
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.message").value(messageService.generateMessage("error.entity.not_found", profileId))
                );
    }

    public void updateProfileForbidden(String profileId, String updateUsername) throws Exception {
        mockMvc.perform(patch(PROFILE_BY_ID_URL.getConstant().formatted(profileId))
                        .content(PROFILE_UPDATE_REQ_PATTERN.getConstant().formatted(updateUsername))
                        .contentType(APPLICATION_JSON)
                        .header("loggedInUser", "dummy email"))
                .andExpectAll(
                        status().isForbidden(),
                        jsonPath("$.message").value(messageService.generateMessage("error.forbidden"))
                );
    }

    private String extractFieldFromResponse(ResultActions resultActions, String field) throws Exception {
        return new ObjectMapper()
                .readTree(
                        resultActions.andReturn()
                                .getResponse()
                                .getContentAsString()
                )
                .at(field)
                .asText();
    }
}
