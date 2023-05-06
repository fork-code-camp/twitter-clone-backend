package com.example.mapper;

import com.example.dto.request.TweetCreateRequest;
import com.example.dto.request.TweetUpdateRequest;
import com.example.dto.response.TweetResponse;
import com.example.entity.Tweet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TweetMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileId", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "likes", ignore = true)
    Tweet toEntity(TweetCreateRequest tweetCreateRequest);

    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "username", ignore = true)
    TweetResponse toResponse(Tweet tweet);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileId", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "likes", ignore = true)
    void updateTweet(TweetUpdateRequest tweetUpdateRequest, @MappingTarget Tweet tweet);
}
