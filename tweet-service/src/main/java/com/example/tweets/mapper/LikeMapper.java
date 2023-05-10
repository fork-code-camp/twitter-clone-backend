package com.example.tweets.mapper;

import com.example.tweets.client.ProfileServiceClient;
import com.example.tweets.entity.Like;
import com.example.tweets.service.TweetService;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LikeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tweet", expression = "java(tweetService.getTweetEntityById(tweetId))")
    @Mapping(target = "profileId", expression = "java(profileServiceClient.getProfileIdByLoggedInUser(loggedInUser))")
    Like toEntityFromTweetId(Long tweetId, @Context TweetService tweetService, @Context ProfileServiceClient profileServiceClient, @Context String loggedInUser);
}
