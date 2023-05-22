package com.example.profile.integration.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum ProfileConstants {
    PROFILE_REQ_PATTERN(
            """
                    {
                        "joinDate": "2000-01-01",
                        "email": "%s",
                        "username": "test-username"
                    }
                    """
    ),
    PROFILE_UPDATE_REQ_PATTERN(
            """
                    {
                        "username": "%s"
                    }
                    """
    ),

    NEW_PROFILE_EMAIL("new_profile@gmail.com"),
    UPDATE_PROFILE_EMAIL("updated@gmail.com"),
    EXISTENT_PROFILE_EMAIL("test@gmail.com");

    private final String constant;
}
