package com.example.fanout.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public enum Operation {

    ADD("ADD"),
    UPDATE("UPDATE"),
    DELETE("DELETE");

    private final String operation;
}
