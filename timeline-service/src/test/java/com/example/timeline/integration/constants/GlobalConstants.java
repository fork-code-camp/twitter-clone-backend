package com.example.timeline.integration.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public enum GlobalConstants {

    USER_TIMELINE_PREFIX("%s_user_timeline:"),
    HOME_TIMELINE_PREFIX("%s_home_timeline:"),

    TWEETS("tweets"),
    RETWEETS("retweets"),
    REPLIES("replies");

    private final String constant;
}
