package com.example.tweet.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EntityName {
    TWEETS("tweets"),
    RETWEETS("retweets"),
    REPLIES("replies");
    private final String name;
}
