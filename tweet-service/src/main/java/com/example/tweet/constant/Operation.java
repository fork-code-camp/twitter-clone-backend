package com.example.tweet.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Operation {

    ADD("ADD"),
    DELETE("DELETE");

    private final String operation;
}
