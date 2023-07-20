package com.example.tweet.integration.controller;

import com.example.tweet.dto.request.TweetCreateRequest;
import com.example.tweet.integration.IntegrationTestBase;
import com.example.tweet.service.MessageSourceService;
import com.example.tweet.service.ReplyService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.tweet.integration.constants.GlobalConstants.*;
import static com.example.tweet.integration.constants.UrlConstants.REPLY_URL_WITH_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@RequiredArgsConstructor
@Sql(value = "classpath:/sql/data.sql")
@SuppressWarnings("SameParameterValue")
public class ReplyControllerTest extends IntegrationTestBase {

    private final MockMvc mockMvc;
    private final ReplyService replyService;
    private final MessageSourceService messageSourceService;

    @Test
    public void replyTest() throws Exception {
        replyAndExpectSuccess(DEFAULT_REPLY_TEXT.getConstant(), 1L, DEFAULT_TWEET_TEXT.getConstant(), 1, 1);
        replyAndExpectSuccess(DEFAULT_REPLY_TEXT.getConstant(), 1L, DEFAULT_TWEET_TEXT.getConstant(), 2, 2);
        replyAndExpectSuccess(DEFAULT_REPLY_TEXT.getConstant(), 3L, DEFAULT_REPLY_TEXT.getConstant(), 1, 3);

        replyAndExpectFailure(
                100L,
                DEFAULT_REPLY_TEXT.getConstant(),
                0,
                3,
                NOT_FOUND,
                "$.message",
                messageSourceService.generateMessage("error.entity.not_found", 100)
        );
        replyAndExpectFailure(
                1L,
                "",
                2,
                3,
                BAD_REQUEST,
                "$.text",
                TEXT_EMPTY_MESSAGE.getConstant()
        );
    }

    @Test
    public void getReplyTest() throws Exception {
        replyDummyTweet(new TweetCreateRequest(DEFAULT_REPLY_TEXT.getConstant()), 1L);

        getReplyAndExpectSuccess(2L, 1L, DEFAULT_REPLY_TEXT.getConstant(), 1, 1);
        getReplyAndExpectFailure(100L, NOT_FOUND, "$.message", messageSourceService.generateMessage("error.entity.not_found", 100));
    }

    @Test
    public void updateReplyTest() throws Exception {
        replyDummyTweet(new TweetCreateRequest(DEFAULT_REPLY_TEXT.getConstant()), 1L);

        updateReplyAndExpectSuccess(2L, UPDATE_REPLY_TEXT.getConstant(), 1);
        updateReplyAndExpectFailure(
                100L,
                UPDATE_REPLY_TEXT.getConstant(),
                NOT_FOUND,
                "$.message",
                messageSourceService.generateMessage("error.entity.not_found", 100)
        );
        updateReplyAndExpectFailure(
                2L,
                "",
                BAD_REQUEST,
                "$.text",
                TEXT_EMPTY_MESSAGE.getConstant()
        );
    }

    @Test
    public void deleteReplyTest() throws Exception {
        replyDummyTweet(new TweetCreateRequest(DEFAULT_REPLY_TEXT.getConstant()), 1L);

        deleteReplyAndExpectSuccess(2L);
        deleteReplyAndExpectFailure(2L, NOT_FOUND, "$.message", messageSourceService.generateMessage("error.entity.not_found", 2));
    }

    private void getReplyAndExpectSuccess(Long replyId, Long replyToId, String replyText, int repliesForTweet, int repliesForUser) throws Exception {
        mockMvc.perform(get(
                        REPLY_URL_WITH_ID.getConstant().formatted(replyId))
                        .header("loggedInUser", EMAIL.getConstant())
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.replyTo.replies").value(repliesForTweet),
                        jsonPath("$.quoteTo").value(IsNull.nullValue()),
                        jsonPath("$.retweetTo").value(IsNull.nullValue()),
                        jsonPath("$.text").value(replyText),
                        jsonPath("$.profile.email").value(EMAIL.getConstant()),
                        jsonPath("$.profile.username").value(USERNAME.getConstant()),
                        jsonPath("$.creationDate").exists(),
                        jsonPath("$.mediaUrls").value(IsNull.nullValue()),
                        jsonPath("$.isBelongs").value(Boolean.TRUE)
                );
        checkNumberOfReplies(replyToId, repliesForTweet, repliesForUser);
    }

    private void getReplyAndExpectFailure(Long replyId, HttpStatus status, String jsonPath, String message) throws Exception {
        mockMvc.perform(get(
                        REPLY_URL_WITH_ID.getConstant().formatted(replyId))
                        .header("loggedInUser", EMAIL.getConstant())
                )
                .andExpectAll(
                        status().is(status.value()),
                        jsonPath(jsonPath).value(message)
                );
    }

    private void deleteReplyAndExpectSuccess(Long replyId) throws Exception {
        mockMvc.perform(delete(
                        REPLY_URL_WITH_ID.getConstant().formatted(replyId))
                        .header("loggedInUser", EMAIL.getConstant())
                )
                .andExpectAll(
                        status().isOk(),
                        content().string("true")
                );
    }

    private void deleteReplyAndExpectFailure(Long replyId, HttpStatus status, String jsonPath, String message) throws Exception {
        mockMvc.perform(delete(
                        REPLY_URL_WITH_ID.getConstant().formatted(replyId))
                        .header("loggedInUser", EMAIL.getConstant())
                )
                .andExpectAll(
                        status().is(status.value()),
                        jsonPath(jsonPath).value(message)
                );
    }

    private void updateReplyAndExpectSuccess(Long replyId, String updatedReplyText, int repliesForTweet) throws Exception {
        mockMvc.perform(multipart(
                        HttpMethod.PATCH,
                        REPLY_URL_WITH_ID.getConstant().formatted(replyId))
                        .file(createRequest(updatedReplyText))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("loggedInUser", EMAIL.getConstant())
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.replyTo.replies").value(repliesForTweet),
                        jsonPath("$.quoteTo").value(IsNull.nullValue()),
                        jsonPath("$.retweetTo").value(IsNull.nullValue()),
                        jsonPath("$.text").value(updatedReplyText),
                        jsonPath("$.profile.email").value(EMAIL.getConstant()),
                        jsonPath("$.profile.username").value(USERNAME.getConstant()),
                        jsonPath("$.creationDate").exists(),
                        jsonPath("$.mediaUrls").value(IsNull.nullValue()),
                        jsonPath("$.isBelongs").value(Boolean.TRUE)
                );
    }

    private void updateReplyAndExpectFailure(Long replyId, String updatedReplyText, HttpStatus status, String jsonPath, String message) throws Exception {
        mockMvc.perform(multipart(
                        HttpMethod.PATCH,
                        REPLY_URL_WITH_ID.getConstant().formatted(replyId))
                        .file(createRequest(updatedReplyText))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("loggedInUser", EMAIL.getConstant())
                )
                .andExpectAll(
                        status().is(status.value()),
                        jsonPath(jsonPath).value(message)
                );
    }

    private void replyAndExpectSuccess(String replyText, Long replyToId, String replyToText, int repliesForTweet, int repliesForUser) throws Exception {
        mockMvc.perform(multipart(
                        HttpMethod.POST,
                        REPLY_URL_WITH_ID.getConstant().formatted(replyToId))
                        .file(createRequest(replyText))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("loggedInUser", EMAIL.getConstant())
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.replyTo.text").value(replyToText),
                        jsonPath("$.replyTo.replies").value(repliesForTweet),
                        jsonPath("$.replyTo.retweets").exists(),
                        jsonPath("$.replyTo.likes").exists(),
                        jsonPath("$.replyTo.views").exists(),
                        jsonPath("$.replyTo.profile").exists(),
                        jsonPath("$.quoteTo").value(IsNull.nullValue()),
                        jsonPath("$.retweetTo").value(IsNull.nullValue()),
                        jsonPath("$.text").value(replyText),
                        jsonPath("$.replies").exists(),
                        jsonPath("$.retweets").exists(),
                        jsonPath("$.likes").exists(),
                        jsonPath("$.views").exists(),
                        jsonPath("$.profile.email").value(EMAIL.getConstant()),
                        jsonPath("$.profile.username").value(USERNAME.getConstant()),
                        jsonPath("$.creationDate").exists(),
                        jsonPath("$.mediaUrls").value(IsNull.nullValue()),
                        jsonPath("$.isBelongs").value(Boolean.TRUE)
                );
        checkNumberOfReplies(replyToId, repliesForTweet, repliesForUser);
    }

    private void replyAndExpectFailure(
            Long replyToId,
            String replyText,
            int repliesForTweet,
            int repliesForUser,
            HttpStatus status,
            String jsonPath,
            String message
    ) throws Exception {
        mockMvc.perform(multipart(
                        HttpMethod.POST,
                        REPLY_URL_WITH_ID.getConstant().formatted(replyToId))
                        .file(createRequest(replyText))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("loggedInUser", EMAIL.getConstant())
                )
                .andExpectAll(
                        status().is(status.value()),
                        jsonPath(jsonPath).value(message)
                );
        checkNumberOfReplies(replyToId, repliesForTweet, repliesForUser);
    }

    private void checkNumberOfReplies(long replyToId, int repliesForTweet, int repliesForUser) {
        try {
            assertEquals(repliesForTweet, replyService.getAllRepliesForTweet(replyToId, EMAIL.getConstant()).size());
        } catch (EntityNotFoundException ignored) {
        }
        assertEquals(repliesForUser, replyService.getAllRepliesForUser(ID.getConstant(), PageRequest.of(0, 20)).size());
    }

    private void replyDummyTweet(TweetCreateRequest request, Long replyToId) {
        replyService.reply(request, replyToId, EMAIL.getConstant(), null);
    }
}
