package com.example.tweets.mapper;

import com.example.tweets.client.ProfileServiceClient;
import com.example.tweets.dto.response.RetweetResponse;
import com.example.tweets.entity.Retweet;
import com.example.tweets.entity.Tweet;
import com.example.tweets.repository.RetweetRepository;
import com.example.tweets.repository.TweetRepository;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RetweetMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentTweet", expression = "java(parentTweet)")
    @Mapping(target = "profileId", expression = "java(profileServiceClient.getProfileIdByLoggedInUser(loggedInUser))")
    @Mapping(target = "retweetTime", expression = "java(java.time.LocalDateTime.now())")
    Retweet toEntity(
            Tweet parentTweet,
            @Context ProfileServiceClient profileServiceClient,
            @Context String loggedInUser
    );

    @Mapping(target = "parentTweet", expression = "java(tweetMapper.toResponse(retweet.getParentTweet(), retweetRepository, tweetRepository, profileServiceClient))")
    @Mapping(target = "profile", expression = "java(profileServiceClient.getProfileById(retweet.getProfileId()))")
    RetweetResponse toResponse(
            Retweet retweet,
            @Context TweetMapper tweetMapper,
            @Context RetweetRepository retweetRepository,
            @Context TweetRepository tweetRepository,
            @Context ProfileServiceClient profileServiceClient
    );
}
