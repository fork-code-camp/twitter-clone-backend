package com.example.tweet.integration.controller;


import com.example.tweet.integration.IntegrationTestBase;
import com.example.tweet.repository.LikeRepository;
import com.example.tweet.service.LikeService;
import com.example.tweet.service.MessageSourceService;
import com.example.tweet.service.TweetService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.tweet.integration.constants.GlobalConstants.EMAIL;
import static com.example.tweet.integration.constants.GlobalConstants.ERROR_DUPLICATE_ENTITY;
import static com.example.tweet.integration.constants.UrlConstants.LIKE_URL_WITH_ID;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@RequiredArgsConstructor
@Sql(value = "classpath:sql/data.sql")
@SuppressWarnings("SameParameterValue")
public class LikeControllerTest extends IntegrationTestBase {

    private final MockMvc mockMvc;
    private final MessageSourceService messageSourceService;
    private final LikeRepository likeRepository;
    private final TweetService tweetService;
    private final LikeService likeService;

    @Test
    public void likeTweetTest() throws Exception {
        likeTweetAndExpectSuccess(1L);

        likeTweetAndExpectFailure(100L, NOT_FOUND, "$.message", messageSourceService.generateMessage("error.entity.not_found", 100));
        likeTweetAndExpectFailure(1L, BAD_REQUEST, "$.message", ERROR_DUPLICATE_ENTITY.getConstant());
    }

    @Test
    public void unlikeTweetTest() throws Exception {
        likeService.likeTweet(1L, EMAIL.getConstant());
        unlikeTweetAndExpectSuccess(1L);

        unlikeTweetAndExpectFailure(100L);
        unlikeTweetAndExpectFailure(1L);
    }


    private void likeTweetAndExpectSuccess(Long tweetId) throws Exception {
        mockMvc.perform(post(
                        LIKE_URL_WITH_ID.getConstant().formatted(tweetId))
                        .header("loggedInUser", EMAIL.getConstant()))
                .andExpectAll(
                        status().isCreated()
                );

        assertTrue(likeRepository.existsById(1L));
        assertTrue(tweetService.getTweet(tweetId, EMAIL.getConstant()).getIsLiked());
    }

    private void likeTweetAndExpectFailure(Long tweetId, HttpStatus status, String jsonPath, String message) throws Exception {
        mockMvc.perform(post(
                        LIKE_URL_WITH_ID.getConstant().formatted(tweetId))
                        .header("loggedInUser", EMAIL.getConstant())
                )
                .andExpectAll(
                        status().is(status.value()),
                        jsonPath(jsonPath).value(containsStringIgnoringCase(message))
                );
    }

    private void unlikeTweetAndExpectSuccess(Long tweetId) throws Exception {
        mockMvc.perform(delete(
                        LIKE_URL_WITH_ID.getConstant().formatted(tweetId))
                        .header("loggedInUser", EMAIL.getConstant())
                )
                .andExpectAll(
                        status().isOk(),
                        content().string("")
                );

        assertFalse(likeRepository.existsById(1L));
        assertFalse(tweetService.getTweet(tweetId, EMAIL.getConstant()).getIsLiked());
    }

    private void unlikeTweetAndExpectFailure(Long tweetId) throws Exception {
        mockMvc.perform(delete(
                        LIKE_URL_WITH_ID.getConstant().formatted(tweetId))
                        .header("loggedInUser", EMAIL.getConstant())
                )
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.message").value(messageSourceService.generateMessage("error.entity.not_found", tweetId))
                );
    }
}
