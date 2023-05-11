package com.example.tweets.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource({
        "classpath:${envTarget:errors}.properties"
})
public class PropertiesConfig {
}
