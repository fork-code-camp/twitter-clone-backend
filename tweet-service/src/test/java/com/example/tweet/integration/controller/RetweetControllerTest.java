package com.example.tweet.integration.controller;

import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.client.StorageServiceClient;
import com.example.tweet.integration.IntegrationTestBase;
import com.example.tweet.integration.mocks.ProfileClientMock;
import com.example.tweet.repository.TweetRepository;
import com.example.tweet.service.MessageSourceService;
import com.example.tweet.service.RetweetService;
import lombok.RequiredArgsConstructor;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.tweet.integration.constants.GlobalConstants.*;
import static com.example.tweet.integration.constants.UrlConstants.RETWEETS_URL_WITH_ID;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@RequiredArgsConstructor
@Sql(value = "classpath:sql/data.sql")
@SuppressWarnings("all")
public class RetweetControllerTest extends IntegrationTestBase {

    private final MockMvc mockMvc;
    private final TweetRepository tweetRepository;
    private final MessageSourceService messageSourceService;
    private final RetweetService retweetService;

    @MockBean
    private final ProfileServiceClient profileServiceClient;

    @MockBean
    private final StorageServiceClient storageServiceClient;

    @BeforeEach
    public void setUp() {
        ProfileClientMock.setupProfileClientResponse(profileServiceClient);
    }

    @Test
    public void retweetTest() throws Exception {
        retweetAndExpectSuccess(1L);

        getRetweetAndExpectSuccess(2L);
        retweetAndExpectFailure(100L, NOT_FOUND, messageSourceService.generateMessage("error.entity.not_found", 100));
        retweetAndExpectFailure(1L, BAD_REQUEST, ERROR_DUPLICATE_ENTITY.getConstant());
    }

    @Test
    public void undoRetweetTest() throws Exception {
        retweetDummyTweet();

        undoRetweetAndExpectSuccess(1L);
        getRetweetAndExpectFailure(2L, NOT_FOUND, messageSourceService.generateMessage("error.entity.not_found", 2));
        undoRetweetAndExpectFailure(1L, NOT_FOUND, messageSourceService.generateMessage("error.entity.not_found", 1));
    }

    @Test
    public void getRetweetTest() throws Exception {
        retweetDummyTweet();

        getRetweetAndExpectSuccess(2L);
        getRetweetAndExpectFailure(100L, NOT_FOUND, messageSourceService.generateMessage("error.entity.not_found", 100));
    }

    private void retweetAndExpectSuccess(Long retweetToId) throws Exception {
        mockMvc.perform(post(
                        RETWEETS_URL_WITH_ID.getConstant().formatted(retweetToId))
                        .header("loggedInUser", EMAIL.getConstant())
                )
                .andExpectAll(
                        status().isOk(),
                        content().string("true")
                );

        assertTrue(tweetRepository.findByIdAndRetweetToIsNotNull(retweetToId+1).isPresent());
    }

    private void retweetAndExpectFailure(Long retweetToId, HttpStatus status, String message) throws Exception {
        mockMvc.perform(post(
                        RETWEETS_URL_WITH_ID.getConstant().formatted(retweetToId))
                        .header("loggedInUser", EMAIL.getConstant())
                )
                .andExpectAll(
                        status().is(status.value()),
                        jsonPath("$.message").value(containsStringIgnoringCase(message))
                );
    }

    private void undoRetweetAndExpectSuccess(Long retweetToId) throws Exception {
        mockMvc.perform(delete(
                        RETWEETS_URL_WITH_ID.getConstant().formatted(retweetToId))
                        .header("loggedInUser", EMAIL.getConstant())
                )
                .andExpectAll(
                        status().isOk(),
                        content().string("true")
                );

        assertFalse(tweetRepository.findByRetweetToIdAndProfileId(retweetToId, ID.getConstant()).isPresent());
    }

    private void undoRetweetAndExpectFailure(Long retweetToId, HttpStatus status, String message) throws Exception {
        mockMvc.perform(delete(
                        RETWEETS_URL_WITH_ID.getConstant().formatted(retweetToId))
                        .header("loggedInUser", EMAIL.getConstant())
                )
                .andExpectAll(
                        status().is(status.value()),
                        jsonPath("$.message").value(message)
                );
    }

    private void getRetweetAndExpectSuccess(Long retweetId) throws Exception {
        mockMvc.perform(get(
                        RETWEETS_URL_WITH_ID.getConstant().formatted(retweetId))
                        .header("loggedInUser", EMAIL.getConstant())
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.quoteTo").value(IsNull.nullValue()),
                        jsonPath("$.replyTo").value(IsNull.nullValue()),
                        jsonPath("$.retweetTo.text").value(DEFAULT_TWEET_TEXT.getConstant()),
                        jsonPath("$.retweetTo.profile.username").value(USERNAME.getConstant()),
                        jsonPath("$.retweetTo.profile.email").value(EMAIL.getConstant()),
                        jsonPath("$.retweetTo.replies").exists(),
                        jsonPath("$.retweetTo.retweets").value(1),
                        jsonPath("$.retweetTo.likes").exists(),
                        jsonPath("$.retweetTo.views").exists(),
                        jsonPath("$.retweetTo.creationDate").exists(),
                        jsonPath("$.retweetTo.isRetweeted").value(Boolean.TRUE),
                        jsonPath("$.profile.username").value(USERNAME.getConstant()),
                        jsonPath("$.profile.email").value(EMAIL.getConstant()),
                        jsonPath("$.mediaUrls").value(IsNull.nullValue()),
                        jsonPath("$.isBelongs").value(Boolean.TRUE)
                );
    }

    private void getRetweetAndExpectFailure(Long retweetId, HttpStatus status, String message) throws Exception {
        mockMvc.perform(get(
                        RETWEETS_URL_WITH_ID.getConstant().formatted(retweetId))
                        .header("loggedInUser", EMAIL.getConstant())
                )
                .andExpectAll(
                        status().is(status.value()),
                        jsonPath("$.message").value(message)
                );
    }

    private void retweetDummyTweet() {
        retweetService.retweet(1L, EMAIL.getConstant());
    }
}
