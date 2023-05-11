package com.example.tweets.mapper;

import com.example.tweets.client.ProfileServiceClient;
import com.example.tweets.dto.request.TweetCreateRequest;
import com.example.tweets.dto.request.TweetUpdateRequest;
import com.example.tweets.dto.response.TweetResponse;
import com.example.tweets.entity.Tweet;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TweetMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileId", expression = "java(profileServiceClient.getProfileIdByLoggedInUser(loggedInUser))")
    @Mapping(target = "creationDate", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "likes", expression = "java(new java.util.ArrayList<>())")
    Tweet toEntity(TweetCreateRequest request, @Context ProfileServiceClient profileServiceClient, @Context String loggedInUser);

    @Mapping(target = "likes", expression = "java(tweet.getLikes().size())")
    @Mapping(target = "username", expression = "java(profileServiceClient.getProfileById(tweet.getProfileId()).username())")
    TweetResponse toResponse(Tweet tweet, @Context ProfileServiceClient profileServiceClient);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileId", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "likes", ignore = true)
    Tweet updateTweet(TweetUpdateRequest request, @MappingTarget Tweet tweet);
}
