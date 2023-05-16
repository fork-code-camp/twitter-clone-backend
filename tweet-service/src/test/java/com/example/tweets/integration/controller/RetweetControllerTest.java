package com.example.tweets.integration.controller;

import com.example.tweets.client.ProfileServiceClient;
import com.example.tweets.dto.request.RetweetRequest;
import com.example.tweets.integration.IntegrationTestBase;
import com.example.tweets.integration.mocks.ProfileClientMock;
import com.example.tweets.repository.RetweetRepository;
import com.example.tweets.service.MessageSourceService;
import com.example.tweets.service.RetweetService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.tweets.integration.constants.GlobalConstants.*;
import static com.example.tweets.integration.constants.JsonConstants.*;
import static com.example.tweets.integration.constants.UrlConstants.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@RequiredArgsConstructor
@Sql(value = "classpath:sql/data.sql")
public class RetweetControllerTest extends IntegrationTestBase {

    private final MockMvc mockMvc;
    private final MessageSourceService messageSourceService;
    private final RetweetRepository retweetRepository;
    private final RetweetService retweetService;

    @MockBean
    private final ProfileServiceClient profileServiceClient;

    @BeforeEach
    public void setUp() {
        ProfileClientMock.setupProfileClientResponse(profileServiceClient);
    }

    @Test
    public void retweetTest() throws Exception {
        retweetAndExpectSuccess(1L);
        assertTrue(retweetRepository.findByOriginalTweetIdAndProfileId(1L, ID.getConstant()).isPresent());

        retweetAndExpectFailure(100L, NOT_FOUND, messageSourceService.generateMessage("error.entity.not_found", 100));
        retweetAndExpectFailure(1L, BAD_REQUEST, ERROR_DUPLICATE_ENTITY.getConstant());
    }

    @Test
    public void undoRetweetTest() throws Exception {
        retweetDummyTweet();

        undoRetweetAndExpectSuccess(1L);
        undoRetweetAndExpectFailure(1L, NOT_FOUND, messageSourceService.generateMessage("error.entity.not_found", 1));

        assertFalse(retweetRepository.findByOriginalTweetIdAndProfileId(1L, ID.getConstant()).isPresent());
    }

    @Test
    public void getRetweetTest() throws Exception {
        retweetDummyTweet();

        getRetweetAndExpectSuccess(1L);
        getRetweetAndExpectFailure(100L, NOT_FOUND, messageSourceService.generateMessage("error.entity.not_found", 100));
    }

    private void retweetAndExpectSuccess(Long tweetId) throws Exception {
        mockMvc.perform(post(
                        RETWEETS_URL_WITH_ID.getConstant().formatted(tweetId))
                        .content(RETWEET_REQUEST.getConstant())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("loggedInUser", EMAIL.getConstant())
                )
                .andExpectAll(
                        status().isOk(),
                        content().string("true")
                );
    }

    private void retweetAndExpectFailure(Long tweetId, HttpStatus status, String message) throws Exception {
        mockMvc.perform(post(
                        RETWEETS_URL_WITH_ID.getConstant().formatted(tweetId))
                        .content(RETWEET_REQUEST.getConstant())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("loggedInUser", EMAIL.getConstant())
                )
                .andExpectAll(
                        status().is(status.value()),
                        jsonPath("$.message").value(containsStringIgnoringCase(message))
                );
    }

    private void undoRetweetAndExpectSuccess(Long tweetId) throws Exception {
        mockMvc.perform(delete(
                        RETWEETS_URL_WITH_ID.getConstant().formatted(tweetId))
                        .header("loggedInUser", EMAIL.getConstant())
                )
                .andExpectAll(
                        status().isOk(),
                        content().string("true")
                );
    }

    private void undoRetweetAndExpectFailure(Long tweetId, HttpStatus status, String message) throws Exception {
        mockMvc.perform(delete(
                        RETWEETS_URL_WITH_ID.getConstant().formatted(tweetId))
                        .header("loggedInUser", EMAIL.getConstant())
                )
                .andExpectAll(
                        status().is(status.value()),
                        jsonPath("$.message").value(message)
                );
    }

    private void getRetweetAndExpectSuccess(Long tweetId) throws Exception {
        mockMvc.perform(get(RETWEETS_URL_WITH_ID.getConstant().formatted(tweetId))
                        .header("loggedInUser", EMAIL.getConstant())
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.originalTweet.text").value(DEFAULT_TWEET_TEXT.getConstant()),
                        jsonPath("$.originalTweet.profile.username").value(USERNAME.getConstant()),
                        jsonPath("$.originalTweet.profile.email").value(EMAIL.getConstant()),
                        jsonPath("$.originalTweet.likes").value(0),
                        jsonPath("$.originalTweet.retweets").value(1),
                        jsonPath("$.originalTweet.creationDate").exists(),
                        jsonPath("$.profile.username").value(USERNAME.getConstant()),
                        jsonPath("$.profile.email").value(EMAIL.getConstant()),
                        jsonPath("$.text").value(DEFAULT_TWEET_TEXT.getConstant()),
                        jsonPath("$.likes").value(0),
                        jsonPath("$.retweets").value(0)
                );
    }

    private void getRetweetAndExpectFailure(Long tweetId, HttpStatus status, String message) throws Exception {
        mockMvc.perform(get(RETWEETS_URL_WITH_ID.getConstant().formatted(tweetId))
                        .header("loggedInUser", EMAIL.getConstant())
                )
                .andExpectAll(
                        status().is(status.value()),
                        jsonPath("$.message").value(message)
                );
    }

    private void retweetDummyTweet() {
        retweetService.retweet(new RetweetRequest(DEFAULT_TWEET_TEXT.getConstant()), 1L, EMAIL.getConstant());
    }
}
