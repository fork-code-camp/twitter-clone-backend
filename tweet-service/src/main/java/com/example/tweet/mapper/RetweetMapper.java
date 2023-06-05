package com.example.tweet.mapper;

import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.dto.response.RetweetResponse;
import com.example.tweet.entity.Retweet;
import com.example.tweet.entity.Tweet;
import com.example.tweet.util.TweetUtil;
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

    @Mapping(target = "parentTweet", expression = "java(tweetMapper.toResponse(retweet.getParentTweet(), tweetUtil, profileServiceClient))")
    @Mapping(target = "profile", expression = "java(profileServiceClient.getProfileById(retweet.getProfileId()))")
    RetweetResponse toResponse(
            Retweet retweet,
            @Context TweetMapper tweetMapper,
            @Context TweetUtil tweetUtil,
            @Context ProfileServiceClient profileServiceClient
    );
}
