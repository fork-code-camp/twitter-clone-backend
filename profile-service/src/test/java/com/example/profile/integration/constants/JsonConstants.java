package com.example.profile.integration.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import static com.example.profile.integration.constants.ProfileConstants.*;

@Getter
@ToString
@AllArgsConstructor
public enum JsonConstants {
    NEW_PROFILE_JSON(PROFILE_REQ_PATTERN.getConstant().formatted(NEW_PROFILE_EMAIL.getConstant())),
    UPDATE_PROFILE_JSON(PROFILE_REQ_PATTERN.getConstant().formatted(UPDATE_PROFILE_EMAIL.getConstant())),
    EXISTENT_PROFILE_JSON(PROFILE_REQ_PATTERN.getConstant().formatted(EXISTENT_PROFILE_EMAIL.getConstant()));

    private final String constant;
}
