package com.example.fanout.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum TimelineCachePrefix {
    USER_TIMELINE_PREFIX("%s_user_timeline:"),
    HOME_TIMELINE_PREFIX("%s_home_timeline:");
    private final String prefix;
}
