package com.example.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageSourceService {

    private final Environment environment;

    public String generateMessage(String source) {
        return environment.getProperty(source);
    }

    public String generateMessage(String source, Object... params) {
        String property = environment.getProperty(source);

        if (property == null) {
            throw new RuntimeException(
                    String.format("property not found %s", source)
            );
        }

        return String.format(source, params);
    }
}
