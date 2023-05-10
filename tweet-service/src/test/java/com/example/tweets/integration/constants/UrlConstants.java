package com.example.tweets.integration.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
public enum UrlConstants {

    TWEETS_URL("/api/v1/tweets"),
    TWEETS_URL_WITH_ID("/api/v1/tweets/%d");

    private final String constant;
}