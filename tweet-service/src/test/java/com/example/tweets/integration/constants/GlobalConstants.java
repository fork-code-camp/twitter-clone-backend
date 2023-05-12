package com.example.tweets.integration.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public enum GlobalConstants {

    EMAIL("dummy-email"),
    USERNAME("dummy-username"),
    ID("dummy-id"),
    DEFAULT_TWEET_TEXT("some text"),
    UPDATE_TWEET_TEXT("updated text"),
    TEXT_EMPTY_MESSAGE("Text shouldn't be empty."),
    ERROR_DUPLICATE_ENTITY("could not execute statement");


    private final String constant;
}
