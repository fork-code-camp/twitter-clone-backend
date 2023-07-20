package com.example.profile.integration.controller;

import com.example.profile.integration.IntegrationTestBase;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.profile.integration.constants.ProfileConstants.*;
import static com.example.profile.integration.constants.UrlConstants.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@RequiredArgsConstructor
public class FollowControllerTest extends IntegrationTestBase {

    private final MockMvc mockMvc;

    @Test
    public void isFollowedTest() throws Exception {
        String profileId = getProfileIdByEmail(EXISTENT_PROFILE_EMAIL.getConstant());
        isFollowedExpect(profileId, NEW_PROFILE_EMAIL.getConstant(), "false");
        followExpect(profileId, NEW_PROFILE_EMAIL.getConstant(), "true");
        followExpect(profileId, NEW_PROFILE_EMAIL.getConstant(), "false");
        isFollowedExpect(profileId, NEW_PROFILE_EMAIL.getConstant(), "true");
        unfollowExpect(profileId, NEW_PROFILE_EMAIL.getConstant(), "true");
        unfollowExpect(profileId, NEW_PROFILE_EMAIL.getConstant(), "false");
        isFollowedExpect(profileId, NEW_PROFILE_EMAIL.getConstant(), "false");
    }

    @Test
    public void getFollowersTest() throws Exception {
        String followeeId = getProfileIdByEmail(UPDATE_PROFILE_EMAIL.getConstant());
        followExpect(followeeId, NEW_PROFILE_EMAIL.getConstant(), "true");
        followExpect(followeeId, EXISTENT_PROFILE_EMAIL.getConstant(), "true");
        getFollowersExpectAmount(followeeId, 2);
        unfollowExpect(followeeId, NEW_PROFILE_EMAIL.getConstant(), "true");
        getFollowersExpectAmount(followeeId, 1);
        unfollowExpect(followeeId, EXISTENT_PROFILE_EMAIL.getConstant(), "true");
        getFollowersExpectAmount(followeeId, 0);
    }

    @Test
    public void getFolloweesTest() throws Exception {
        String firstFolloweeId = getProfileIdByEmail(EXISTENT_PROFILE_EMAIL.getConstant());
        String secondFolloweeId = getProfileIdByEmail(NEW_PROFILE_EMAIL.getConstant());
        followExpect(firstFolloweeId, UPDATE_PROFILE_EMAIL.getConstant(), "true");
        followExpect(secondFolloweeId, UPDATE_PROFILE_EMAIL.getConstant(), "true");
        String followerId = getProfileIdByEmail(UPDATE_PROFILE_EMAIL.getConstant());
        getFolloweesExpectAmount(followerId, 2);
        unfollowExpect(firstFolloweeId, UPDATE_PROFILE_EMAIL.getConstant(), "true");
        getFolloweesExpectAmount(followerId, 1);
        unfollowExpect(secondFolloweeId, UPDATE_PROFILE_EMAIL.getConstant(), "true");
        getFolloweesExpectAmount(followerId, 0);
    }

    private void isFollowedExpect(String followeeId, String loggedInUser, String expected) throws Exception {
        mockMvc.perform(get(FOLLOW_BY_ID_URL.getConstant().formatted(followeeId))
                        .header("loggedInUser", loggedInUser))
                .andExpectAll(
                        status().is2xxSuccessful(),
                        content().string(expected)
                );
    }

    private void followExpect(String followeeId, String loggedInUser, String expected) throws Exception {
        mockMvc.perform(post(FOLLOW_BY_ID_URL.getConstant().formatted(followeeId))
                        .header("loggedInUser", loggedInUser))
                .andExpectAll(
                        status().is2xxSuccessful(),
                        content().string(expected)
                );
    }

    private void unfollowExpect(String followeeId, String loggedInUser, String expected) throws Exception {
        mockMvc.perform(delete(FOLLOW_BY_ID_URL.getConstant().formatted(followeeId))
                        .header("loggedInUser", loggedInUser))
                .andExpectAll(
                        status().is2xxSuccessful(),
                        content().string(expected)
                );
    }

    private void getFollowersExpectAmount(String profileId, Integer amount) throws Exception {
        mockMvc.perform(get(FOLLOWERS_BY_ID_URL.getConstant().formatted(profileId)))
                .andExpectAll(
                        status().is2xxSuccessful(),
                        jsonPath("$", hasSize(amount))
                );
    }

    private void getFolloweesExpectAmount(String profileId, Integer amount) throws Exception {
        mockMvc.perform(get(FOLLOWEES_BY_ID_URL.getConstant().formatted(profileId)))
                .andExpectAll(
                        status().is2xxSuccessful(),
                        jsonPath("$", hasSize(amount))
                );
    }

    private String getProfileIdByEmail(String email) throws Exception {
        return mockMvc.perform(get(PROFILE_ID_BY_EMAIL_URL.getConstant().formatted(email)))
                .andExpectAll(
                        status().is2xxSuccessful(),
                        content().string(not(emptyString())),
                        content().string(hasLength(24))
                )
                .andReturn()
                .getResponse()
                .getContentAsString();
    }
}
