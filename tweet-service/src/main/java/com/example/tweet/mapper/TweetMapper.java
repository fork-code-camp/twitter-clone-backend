package com.example.tweet.mapper;

import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.dto.request.TweetCreateRequest;
import com.example.tweet.dto.request.TweetUpdateRequest;
import com.example.tweet.dto.response.TweetResponse;
import com.example.tweet.entity.Tweet;
import com.example.tweet.util.TweetUtil;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TweetMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "text", expression = "java(request.text())")
    @Mapping(target = "profileId", expression = "java(profileServiceClient.getProfileIdByLoggedInUser(loggedInUser))")
    @Mapping(target = "creationDate", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "mediaUrls", ignore = true)
    @Mapping(target = "likes", expression = "java(new java.util.HashSet<>())")
    @Mapping(target = "retweets", expression = "java(new java.util.HashSet<>())")
    @Mapping(target = "replies", expression = "java(new java.util.HashSet<>())")
    @Mapping(target = "views", expression = "java(new java.util.HashSet<>())")
    @Mapping(target = "retweetTo", ignore = true)
    @Mapping(target = "quoteTo", expression = "java(quoteTo)")
    @Mapping(target = "replyTo", expression = "java(replyTo)")
    Tweet toEntity(
            TweetCreateRequest request,
            Tweet quoteTo,
            Tweet replyTo,
            @Context ProfileServiceClient profileServiceClient,
            @Context String loggedInUser
    );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "text", ignore = true)
    @Mapping(target = "profileId", expression = "java(profileServiceClient.getProfileIdByLoggedInUser(loggedInUser))")
    @Mapping(target = "creationDate", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "mediaUrls", ignore = true)
    @Mapping(target = "likes", expression = "java(new java.util.HashSet<>())")
    @Mapping(target = "retweets", expression = "java(new java.util.HashSet<>())")
    @Mapping(target = "replies", expression = "java(new java.util.HashSet<>())")
    @Mapping(target = "views", expression = "java(new java.util.HashSet<>())")
    @Mapping(target = "retweetTo", expression = "java(retweetTo)")
    @Mapping(target = "quoteTo", ignore = true)
    @Mapping(target = "replyTo", ignore = true)
    Tweet toEntity(
            Tweet retweetTo,
            @Context ProfileServiceClient profileServiceClient,
            @Context String loggedInUser
    );

    @Mapping(target = "profile", expression = "java(profileServiceClient.getProfileById(tweet.getProfileId()))")
    @Mapping(target = "quoteTo", expression = "java(this.toResponse(tweet.getQuoteTo(), loggedInUser, tweetUtil, profileServiceClient))")
    @Mapping(target = "replyTo", expression = "java(this.toResponse(tweet.getReplyTo(), loggedInUser, tweetUtil, profileServiceClient))")
    @Mapping(target = "retweetTo", expression = "java(this.toResponse(tweet.getRetweetTo(), loggedInUser, tweetUtil, profileServiceClient))")
    @Mapping(target = "likes", expression = "java(tweetUtil.countLikesForTweet(tweet.getId()))")
    @Mapping(target = "replies", expression = "java(tweetUtil.countRepliesForTweet(tweet.getId()))")
    @Mapping(target = "views", expression = "java(tweetUtil.countViewsForTweet(tweet.getId()))")
    @Mapping(target = "retweets", expression = "java(tweetUtil.countRetweetsForTweet(tweet.getId()))")
    @Mapping(target = "isRetweeted", expression = "java(tweetUtil.isTweetRetweetedByLoggedInUser(tweet.getId(), loggedInUser, profileServiceClient))")
    @Mapping(target = "isLiked", expression = "java(tweetUtil.isTweetLikedByLoggedInUser(tweet.getId(), loggedInUser, profileServiceClient))")
    TweetResponse toResponse(
            Tweet tweet,
            @Context String loggedInUser,
            @Context TweetUtil tweetUtil,
            @Context ProfileServiceClient profileServiceClient
    );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileId", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "mediaUrls", ignore = true)
    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "retweets", ignore = true)
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "replyTo", ignore = true)
    @Mapping(target = "retweetTo", ignore = true)
    @Mapping(target = "quoteTo", ignore = true)
    Tweet updateTweet(TweetUpdateRequest request, @MappingTarget Tweet tweet);
}
