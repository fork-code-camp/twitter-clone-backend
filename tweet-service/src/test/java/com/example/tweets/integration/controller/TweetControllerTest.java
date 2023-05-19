package com.example.tweets.integration.controller;

import com.example.tweets.client.ProfileServiceClient;
import com.example.tweets.dto.request.TweetCreateRequest;
import com.example.tweets.integration.IntegrationTestBase;
import com.example.tweets.integration.mocks.ProfileClientMock;
import com.example.tweets.repository.TweetRepository;
import com.example.tweets.service.MessageSourceService;
import com.example.tweets.service.TweetService;
import lombok.RequiredArgsConstructor;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static com.example.tweets.integration.constants.GlobalConstants.*;
import static com.example.tweets.integration.constants.JsonConstants.REQUEST_PATTERN;
import static com.example.tweets.integration.constants.UrlConstants.TWEETS_URL;
import static com.example.tweets.integration.constants.UrlConstants.TWEETS_URL_WITH_ID;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@RequiredArgsConstructor
@Sql(statements = "ALTER SEQUENCE tweets_id_seq RESTART WITH 1;")
public class TweetControllerTest extends IntegrationTestBase {

    private final MockMvc mockMvc;
    private final MessageSourceService messageSourceService;
    private final TweetService tweetService;
    private final TweetRepository tweetRepository;

    @MockBean
    private final ProfileServiceClient profileServiceClient;

    @BeforeEach
    public void setUp() {
        ProfileClientMock.setupProfileClientResponse(profileServiceClient);
    }

    @Test
    public void createTweetTest() throws Exception {
        createTweetAndExpectSuccess(DEFAULT_TWEET_TEXT.getConstant());
        createTweetAndExpectFailure("");

        createQuoteTweetAndExpectSuccess(DEFAULT_TWEET_TEXT.getConstant(), 1L);
        createQuoteTweetAndExpectSuccess(DEFAULT_TWEET_TEXT.getConstant(), 1L);
        createQuoteTweetAndExpectFailure(
                DEFAULT_TWEET_TEXT.getConstant(),
                100L,
                NOT_FOUND,
                "$.message",
                messageSourceService.generateMessage("error.entity.not_found", 100)
        );
        createQuoteTweetAndExpectFailure(
                "",
                1L,
                BAD_REQUEST,
                "$.text",
                TEXT_EMPTY_MESSAGE.getConstant()
        );

        assertTrue(tweetRepository.existsById(1L));
        assertTrue(tweetRepository.existsById(2L));
        assertTrue(tweetRepository.existsById(3L));
    }

    @Test
    public void getTweetTest() throws Exception {
        createDummyTweet();

        getTweetAndExpectSuccess(1L);
        getTweetAndExpectSuccess(1L);
        getTweetAndExpectFailure(100L);
    }

    @Test
    public void updateTweetTest() throws Exception {
        createDummyTweet();

        updateTweetAndExpectSuccess(1L, UPDATE_TWEET_TEXT.getConstant());
        updateTweetAndExpectFailure(
                1L,
                "",
                BAD_REQUEST,
                "$.text",
                TEXT_EMPTY_MESSAGE.getConstant()
        );
        updateTweetAndExpectFailure(
                100L,
                UPDATE_TWEET_TEXT.getConstant(),
                NOT_FOUND,
                "$.message",
                messageSourceService.generateMessage("error.entity.not_found", 100)
        );
    }

    @Test
    public void deleteTweetTest() throws Exception {
        createDummyTweet();

        deleteTweet(100L, false);
        deleteTweet(1L, true);
    }

    private void createTweetAndExpectSuccess(String text) throws Exception {
        ResultActions resultActions = mockMvc.perform(post(
                TWEETS_URL.getConstant())
                .content(REQUEST_PATTERN.getConstant().formatted(text))
                .contentType(MediaType.APPLICATION_JSON)
                .header("loggedInUser", EMAIL.getConstant()));

        expectOkTweetResponse(resultActions, text, 0);
    }

    private void createTweetAndExpectFailure(String text) throws Exception {
        ResultActions resultActions = mockMvc.perform(post(
                TWEETS_URL.getConstant())
                .content(REQUEST_PATTERN.getConstant().formatted(text))
                .contentType(MediaType.APPLICATION_JSON)
                .header("loggedInUser", EMAIL.getConstant()));

        expectFailResponse(resultActions, BAD_REQUEST, "$.text", TEXT_EMPTY_MESSAGE.getConstant());
    }

    private void createQuoteTweetAndExpectSuccess(String text, Long embeddedTweetId) throws Exception {
        ResultActions resultActions = mockMvc.perform(post(
                TWEETS_URL_WITH_ID.getConstant().formatted(embeddedTweetId))
                .content(REQUEST_PATTERN.getConstant().formatted(text))
                .contentType(MediaType.APPLICATION_JSON)
                .header("loggedInUser", EMAIL.getConstant())
        );

        expectOkQuoteTweetResponse(resultActions, text, 0);
    }

    private void createQuoteTweetAndExpectFailure(String text, Long embeddedTweetId, HttpStatus status, String jsonPath, String message) throws Exception {
        ResultActions resultActions = mockMvc.perform(post(
                TWEETS_URL_WITH_ID.getConstant().formatted(embeddedTweetId))
                .content(REQUEST_PATTERN.getConstant().formatted(text))
                .contentType(MediaType.APPLICATION_JSON)
                .header("loggedInUser", EMAIL.getConstant())
        );

        expectFailResponse(resultActions, status, jsonPath, message);
    }

    private void getTweetAndExpectSuccess(Long id) throws Exception {
        ResultActions resultActions = mockMvc.perform(get(
                TWEETS_URL.getConstant())
                .queryParam("tweetId", String.valueOf(id))
                .header("loggedInUser", EMAIL.getConstant()));

        expectOkTweetResponse(resultActions, DEFAULT_TWEET_TEXT.getConstant(), 1);
    }

    private void getTweetAndExpectFailure(Long id) throws Exception {
        ResultActions resultActions = mockMvc.perform(get(
                TWEETS_URL.getConstant())
                .queryParam("tweetId", String.valueOf(id))
                .header("loggedInUser", EMAIL.getConstant()));

        expectFailResponse(resultActions, NOT_FOUND, "$.message", messageSourceService.generateMessage("error.entity.not_found", id));
    }

    private void updateTweetAndExpectSuccess(Long id, String text) throws Exception {
        ResultActions resultActions = mockMvc.perform(patch(
                TWEETS_URL_WITH_ID.getConstant().formatted(id))
                .content(REQUEST_PATTERN.getConstant().formatted(text))
                .contentType(MediaType.APPLICATION_JSON)
                .header("loggedInUser", EMAIL.getConstant()));

        expectOkTweetResponse(resultActions, text, 0);
    }

    private void updateTweetAndExpectFailure(Long id, String text, HttpStatus status, String jsonPath, String message) throws Exception {
        ResultActions resultActions = mockMvc.perform(patch(
                TWEETS_URL_WITH_ID.getConstant().formatted(id))
                .content(REQUEST_PATTERN.getConstant().formatted(text))
                .contentType(MediaType.APPLICATION_JSON)
                .header("loggedInUser", EMAIL.getConstant()));

        expectFailResponse(resultActions, status, jsonPath, message);
    }

    private void deleteTweet(Long id, Boolean value) throws Exception {
        mockMvc.perform(delete(
                        TWEETS_URL_WITH_ID.getConstant().formatted(id))
                        .header("loggedInUser", EMAIL.getConstant()))
                .andExpectAll(
                        status().isOk(),
                        content().string(value.toString())
                );

        assertFalse(tweetRepository.existsById(id));
    }

    private void expectOkTweetResponse(ResultActions resultActions, String text, int views) throws Exception {
        resultActions
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.parentTweetForReply").value(IsNull.nullValue()),
                        jsonPath("$.embeddedTweet").value(IsNull.nullValue()),
                        jsonPath("$.profile.username").value(USERNAME.getConstant()),
                        jsonPath("$.profile.email").value(EMAIL.getConstant()),
                        jsonPath("$.text").value(text),
                        jsonPath("$.replies").value(0),
                        jsonPath("$.retweets").value(0),
                        jsonPath("$.likes").value(0),
                        jsonPath("$.views").value(views),
                        jsonPath("$.creationDate").exists()
                );
    }

    private void expectOkQuoteTweetResponse(ResultActions resultActions, String text, int views) throws Exception {
        resultActions
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.parentTweetForReply").value(IsNull.nullValue()),
                        jsonPath("$.embeddedTweet.text").value(DEFAULT_TWEET_TEXT.getConstant()),
                        jsonPath("$.embeddedTweet.replies").value(0),
                        jsonPath("$.embeddedTweet.retweets").value(0),
                        jsonPath("$.embeddedTweet.likes").value(0),
                        jsonPath("$.embeddedTweet.views").value(0),
                        jsonPath("$.embeddedTweet.creationDate").exists(),
                        jsonPath("$.profile.username").value(USERNAME.getConstant()),
                        jsonPath("$.profile.email").value(EMAIL.getConstant()),
                        jsonPath("$.text").value(text),
                        jsonPath("$.replies").value(0),
                        jsonPath("$.retweets").value(0),
                        jsonPath("$.likes").value(0),
                        jsonPath("$.views").value(views),
                        jsonPath("$.creationDate").exists()
                );
    }

    private void expectFailResponse(ResultActions resultActions, HttpStatus status, String jsonPath, String message) throws Exception {
        resultActions
                .andExpectAll(
                        status().is(status.value()),
                        jsonPath(jsonPath).value(message)
                );
    }

    private void createDummyTweet() {
        tweetService.createTweet(new TweetCreateRequest(DEFAULT_TWEET_TEXT.getConstant()), EMAIL.getConstant());
    }
}
