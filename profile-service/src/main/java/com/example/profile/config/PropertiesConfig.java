package com.example.profile.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource({
        "classpath:${envTarget:errors}.properties"
})
public class PropertiesConfig {
}
