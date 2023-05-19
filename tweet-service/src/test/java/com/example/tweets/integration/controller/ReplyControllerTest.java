package com.example.tweets.integration.controller;

import com.example.tweets.client.ProfileServiceClient;
import com.example.tweets.integration.IntegrationTestBase;
import com.example.tweets.integration.mocks.ProfileClientMock;
import com.example.tweets.service.MessageSourceService;
import com.example.tweets.service.ReplyService;
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

import static com.example.tweets.integration.constants.GlobalConstants.*;
import static com.example.tweets.integration.constants.JsonConstants.REQUEST_PATTERN;
import static com.example.tweets.integration.constants.UrlConstants.REPLIES_URL_WITH_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@RequiredArgsConstructor
@Sql(value = "classpath:/sql/data.sql")
public class ReplyControllerTest extends IntegrationTestBase {

    private final MockMvc mockMvc;
    private final ReplyService replyService;
    private final MessageSourceService messageSourceService;

    @MockBean
    private final ProfileServiceClient profileServiceClient;

    @BeforeEach
    public void setUp() {
        ProfileClientMock.setupProfileClientResponse(profileServiceClient);
    }

    @Test
    public void replyTest() throws Exception {
        replyAndExpectSuccess(1L, DEFAULT_TWEET_TEXT.getConstant(), 1, 1);
        replyAndExpectSuccess(1L, DEFAULT_TWEET_TEXT.getConstant(), 2, 2);
        replyAndExpectSuccess(3L, DEFAULT_REPLY_TEXT.getConstant(), 1, 3);

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

    private void replyAndExpectSuccess(Long parentTweetId, String parentTweetText, int repliesForTweet, int repliesForUser) throws Exception {
        mockMvc.perform(post(
                        REPLIES_URL_WITH_ID.getConstant().formatted(parentTweetId))
                        .header("loggedInUser", EMAIL.getConstant())
                        .content(REQUEST_PATTERN.getConstant().formatted(DEFAULT_REPLY_TEXT.getConstant()))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.parentTweetForReply.text").value(parentTweetText),
                        jsonPath("$.parentTweetForReply.embeddedTweet").value(IsNull.nullValue()),
                        jsonPath("$.parentTweetForReply.likes").value(0),
                        jsonPath("$.parentTweetForReply.retweets").value(0),
                        jsonPath("$.parentTweetForReply.replies").value(repliesForTweet),
                        jsonPath("$.parentTweetForReply.profile").exists(),
                        jsonPath("$.text").value(DEFAULT_REPLY_TEXT.getConstant()),
                        jsonPath("$.embeddedTweet").value(IsNull.nullValue()),
                        jsonPath("$.likes").value(0),
                        jsonPath("$.retweets").value(0),
                        jsonPath("$.replies").value(0),
                        jsonPath("$.profile.email").value(EMAIL.getConstant()),
                        jsonPath("$.profile.username").value(USERNAME.getConstant()),
                        jsonPath("$.creationDate").exists()
                );
        checkNumberOfReplies(parentTweetId, repliesForTweet, repliesForUser);
    }

    private void replyAndExpectFailure(
            Long parentTweetId,
            String text,
            int repliesForTweet,
            int repliesForUser,
            HttpStatus status,
            String jsonPath,
            String message
    ) throws Exception {
        mockMvc.perform(post(
                        REPLIES_URL_WITH_ID.getConstant().formatted(parentTweetId))
                        .header("loggedInUser", EMAIL.getConstant())
                        .content(REQUEST_PATTERN.getConstant().formatted(text))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(
                        status().is(status.value()),
                        jsonPath(jsonPath).value(message)
                );
        checkNumberOfReplies(parentTweetId, repliesForTweet, repliesForUser);
    }

    private void checkNumberOfReplies(long parentTweetId, int repliesForTweet, int repliesForUser) {
        assertEquals(repliesForTweet, replyService.findAllRepliesForTweet(parentTweetId).size());
        assertEquals(repliesForUser, replyService.findAllRepliesForUser(EMAIL.getConstant()).size());
    }
}
