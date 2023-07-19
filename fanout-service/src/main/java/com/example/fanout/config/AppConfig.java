package com.example.fanout.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
@EnableAsync
public class AppConfig {

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor kafkaExecutor = new ThreadPoolTaskExecutor();
        kafkaExecutor.setCorePoolSize(10);
        kafkaExecutor.setMaxPoolSize(10);
        kafkaExecutor.setQueueCapacity(10);
        kafkaExecutor.setThreadNamePrefix("KafkaConsumer-");
        kafkaExecutor.initialize();
        return kafkaExecutor;
    }

    @Bean
    public Gson gson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, localDateTypeAdapter())
                .registerTypeAdapter(LocalDateTime.class, localDateTimeTypeAdapter())
                .create();
    }

    @Bean
    public TypeAdapter<LocalDateTime> localDateTimeTypeAdapter() {
        return new TypeAdapter<>() {
            @Override
            public void write(JsonWriter out, LocalDateTime value) throws IOException {
                if (value == null) {
                    out.nullValue();
                    return;
                }
                out.value(value.toString());
            }

            @Override
            public LocalDateTime read(JsonReader in) throws IOException {
                if (in.peek() == JsonToken.NULL) {
                    in.nextNull();
                    return null;
                }
                return LocalDateTime.parse(in.nextString());
            }
        };
    }

    @Bean
    public TypeAdapter<LocalDate> localDateTypeAdapter() {
        return new TypeAdapter<>() {
            @Override
            public void write(JsonWriter out, LocalDate value) throws IOException {
                if (value == null) {
                    out.nullValue();
                    return;
                }
                out.value(value.toString());
            }

            @Override
            public LocalDate read(JsonReader in) throws IOException {
                if (in.peek() == JsonToken.NULL) {
                    in.nextNull();
                    return null;
                }
                return LocalDate.parse(in.nextString());
            }
        };
    }
}
