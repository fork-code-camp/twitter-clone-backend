package com.example.fanout.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TweetResponse {

    private Long id;
    private TweetResponse replyTo;
    private TweetResponse retweetTo;
    private ProfileResponse profile;
    private String text;
    private Set<String> mediaUrls;
    private TweetResponse quoteTo;
    private Integer retweets;
    private Integer replies;
    private Integer likes;
    private Integer views;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime creationDate;
    private Boolean isRetweeted;
    private Boolean isLiked;
    private Boolean isBelongs;
}
