package com.example.mapper;

import com.example.client.ProfileClient;
import com.example.dto.request.TweetCreateRequest;
import com.example.dto.request.TweetUpdateRequest;
import com.example.dto.response.TweetResponse;
import com.example.entity.Tweet;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TweetMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileId", expression = "java(profileClient.getProfileIdByLoggedInUser(loggedInUser))")
    @Mapping(target = "creationDate", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "likes", expression = "java(new java.util.ArrayList<>())")
    Tweet toEntity(TweetCreateRequest request, @Context ProfileClient profileClient, @Context String loggedInUser);

    @Mapping(target = "likes", expression = "java(tweet.getLikes().size())")
    @Mapping(target = "username", expression = "java(profileClient.getProfileById(tweet.getProfileId()).username())")
    TweetResponse toResponse(Tweet tweet, @Context ProfileClient profileClient);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileId", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "likes", ignore = true)
    Tweet updateTweet(TweetUpdateRequest request, @MappingTarget Tweet tweet);
}
