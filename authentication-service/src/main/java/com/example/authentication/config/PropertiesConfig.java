package com.example.authentication.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource({
        "classpath:${envTarget:errors}.properties",
        "classpath:${envTarget:messages}.properties"
})
public class PropertiesConfig {
}
