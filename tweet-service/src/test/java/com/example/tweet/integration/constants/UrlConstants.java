package com.example.tweet.integration.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
public enum UrlConstants {

    TWEET_URL("/api/v1/tweet"),
    TWEETS_URL("/api/v1/tweets"),
    TWEET_URL_WITH_ID("/api/v1/tweet/%d"),
    TWEETS_URL_WITH_ID("/api/v1/tweets/%d"),
    LIKE_URL_WITH_ID("/api/v1/like/%d"),
    RETWEET_URL("/api/v1/retweet"),
    RETWEET_URL_WITH_ID("/api/v1/retweet/%d"),
    REPLY_URL_WITH_ID("/api/v1/reply/%d"),
    REPLIES_URL_WITH_ID("/api/v1/replies/%d");

    private final String constant;
}
