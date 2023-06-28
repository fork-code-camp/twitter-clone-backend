package com.example.timeline.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public enum EntityName {
    TWEETS("tweets"),
    RETWEETS("retweets"),
    REPLIES("replies");
    private final String name;
}
