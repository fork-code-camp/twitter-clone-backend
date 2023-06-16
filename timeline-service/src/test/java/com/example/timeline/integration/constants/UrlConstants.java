package com.example.timeline.integration.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
public enum UrlConstants {

    USER_TIMELINE_URL("/api/v1/timelines/user"),
    USER_REPLIES_TIMELINE_URL("/api/v1/timelines/user-replies"),
    HOME_TIMELINE_URL("/api/v1/timelines/home");

    private final String constant;
}
