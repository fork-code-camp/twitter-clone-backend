package com.example.timeline.integration.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
public enum UrlConstants {

    USER_TIMELINE_URL("/api/v1/timeline/user"),
    USER_TIMELINE_URL_FOR_USER("/api/v1/timeline/user/%s"),
    USER_REPLIES_TIMELINE_URL("/api/v1/timeline/user-replies"),
    USER_REPLIES_TIMELINE_URL_FOR_USER("/api/v1/timeline/user-replies/%s"),
    HOME_TIMELINE_URL("/api/v1/timeline/home");

    private final String constant;
}
