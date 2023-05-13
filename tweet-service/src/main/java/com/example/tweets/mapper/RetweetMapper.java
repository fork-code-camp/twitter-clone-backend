package com.example.tweets.mapper;

import com.example.tweets.client.ProfileServiceClient;
import com.example.tweets.dto.response.RetweetResponse;
import com.example.tweets.entity.Retweet;
import com.example.tweets.entity.Tweet;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RetweetMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tweet", source = "tweet")
    @Mapping(target = "profileId", expression = "java(profileServiceClient.getProfileIdByLoggedInUser(loggedInUser))")
    @Mapping(target = "retweetTime", expression = "java(java.time.LocalDateTime.now())")
    Retweet toEntity(
            Tweet tweet,
            @Context ProfileServiceClient profileServiceClient,
            @Context String loggedInUser
    );

    @Mapping(target = "tweet", expression = "java(tweetMapper.toResponse(tweet, profileServiceClient))")
    @Mapping(target = "username", expression = "java(profileServiceClient.getProfileById(retweet.getProfileId()).username())")
    RetweetResponse toResponse(
            Retweet retweet,
            @Context Tweet tweet,
            @Context TweetMapper tweetMapper,
            @Context ProfileServiceClient profileServiceClient
    );
}
