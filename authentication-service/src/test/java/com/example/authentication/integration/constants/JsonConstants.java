package com.example.authentication.integration.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import static com.example.authentication.integration.constants.AuthConstants.*;

@Getter
@ToString
@AllArgsConstructor
public enum JsonConstants {
    EXISTENT_ACCOUNT_JSON (AUTH_REQ_PATTERN.getConstant().formatted(EXISTENT_ACCOUNT_EMAIL.getConstant())),
    NEW_ACCOUNT_JSON (AUTH_REQ_PATTERN.getConstant().formatted(NEW_ACCOUNT_EMAIL.getConstant()));

    private final String constant;
}
