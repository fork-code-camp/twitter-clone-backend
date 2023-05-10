package com.example.tweets.integration.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import static com.example.tweets.integration.constants.TweetConstants.*;

@Getter
@ToString
@AllArgsConstructor
public enum JsonConstants {

    REQUEST_PATTERN("{\"text\": \"%s\"}"),
    CREATE_TWEET_REQUEST(REQUEST_PATTERN.getConstant().formatted(DEFAULT_TWEET_TEXT.getConstant())),
    UPDATE_TWEET_REQUEST(REQUEST_PATTERN.getConstant().formatted(UPDATE_TWEET_TEXT.getConstant())),
    EMPTY_TWEET_REQUEST(REQUEST_PATTERN.getConstant().formatted(""));

    private final String constant;
}
