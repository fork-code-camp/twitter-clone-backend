package com.example.tweet.integration.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum JsonConstants {

    REQUEST_PATTERN(
            """
                    {
                        "text": "%s"
                    }
                    """
    );

    private final String constant;
}
