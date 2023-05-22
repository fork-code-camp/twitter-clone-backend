package com.example.profile.integration.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum UrlConstants {
    PROFILE_URL("/api/v1/profiles"),
    PROFILE_BY_ID_URL("/api/v1/profiles/%s"),
    PROFILE_ID_BY_EMAIL_URL("/api/v1/profiles/id/%s"),

    FOLLOW_BY_ID_URL("/api/v1/follows/%s"),
    FOLLOWERS_BY_ID_URL("/api/v1/follows/followers/%s"),
    FOLLOWEES_BY_ID_URL("/api/v1/follows/followees/%s");

    private final String constant;
}
