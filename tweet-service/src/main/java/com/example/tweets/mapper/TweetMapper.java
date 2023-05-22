package com.example.tweets.mapper;

import com.example.tweets.client.ProfileServiceClient;
import com.example.tweets.dto.request.TweetCreateRequest;
import com.example.tweets.dto.request.TweetUpdateRequest;
import com.example.tweets.dto.response.TweetResponse;
import com.example.tweets.entity.Tweet;
import com.example.tweets.util.TweetUtil;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TweetMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "text", expression = "java(request.text())")
    @Mapping(target = "profileId", expression = "java(profileServiceClient.getProfileIdByLoggedInUser(loggedInUser))")
    @Mapping(target = "quoteTo", expression = "java(quoteTo)")
    @Mapping(target = "replyTo", expression = "java(replyTo)")
    @Mapping(target = "creationDate", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "likes", expression = "java(new java.util.HashSet<>())")
    @Mapping(target = "retweets", expression = "java(new java.util.HashSet<>())")
    @Mapping(target = "replies", expression = "java(new java.util.HashSet<>())")
    Tweet toEntity(
            TweetCreateRequest request,
            Tweet quoteTo,
            Tweet replyTo,
            @Context ProfileServiceClient profileServiceClient,
            @Context String loggedInUser
    );

    @Mapping(target = "profile", expression = "java(profileServiceClient.getProfileById(tweet.getProfileId()))")
    @Mapping(target = "quoteTo", expression = "java(this.toResponse(tweet.getQuoteTo(), tweetUtil, profileServiceClient))")
    @Mapping(target = "replyTo", expression = "java(this.toResponse(tweet.getReplyTo(), tweetUtil, profileServiceClient))")
    @Mapping(target = "likes", expression = "java(tweetUtil.countLikesForTweet(tweet.getId()))")
    @Mapping(target = "replies", expression = "java(tweetUtil.countRepliesForTweet(tweet.getId()))")
    @Mapping(target = "views", expression = "java(tweetUtil.countViewsForTweet(tweet.getId()))")
    @Mapping(target = "retweets", expression = "java(tweetUtil.countRetweetsForTweet(tweet.getId()))")
    TweetResponse toResponse(
            Tweet tweet,
            @Context TweetUtil tweetUtil,
            @Context ProfileServiceClient profileServiceClient
    );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileId", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "retweets", ignore = true)
    @Mapping(target = "replyTo", ignore = true)
    @Mapping(target = "quoteTo", ignore = true)
    Tweet updateTweet(TweetUpdateRequest request, @MappingTarget Tweet tweet);
}
