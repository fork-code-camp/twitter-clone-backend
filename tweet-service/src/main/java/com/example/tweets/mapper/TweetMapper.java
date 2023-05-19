package com.example.tweets.mapper;

import com.example.tweets.client.ProfileServiceClient;
import com.example.tweets.dto.request.TweetCreateRequest;
import com.example.tweets.dto.request.TweetUpdateRequest;
import com.example.tweets.dto.response.TweetResponse;
import com.example.tweets.entity.Tweet;
import com.example.tweets.repository.RetweetRepository;
import com.example.tweets.repository.TweetRepository;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TweetMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "text", expression = "java(request.text())")
    @Mapping(target = "profileId", expression = "java(profileServiceClient.getProfileIdByLoggedInUser(loggedInUser))")
    @Mapping(target = "embeddedTweet", expression = "java(embeddedTweet)")
    @Mapping(target = "parentTweetForReply", expression = "java(parentTweetForReply)")
    @Mapping(target = "creationDate", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "likes", expression = "java(new java.util.ArrayList<>())")
    @Mapping(target = "retweets", expression = "java(new java.util.ArrayList<>())")
    @Mapping(target = "views", expression = "java(new java.util.ArrayList<>())")
    Tweet toEntity(
            TweetCreateRequest request,
            Tweet embeddedTweet,
            Tweet parentTweetForReply,
            @Context ProfileServiceClient profileServiceClient,
            @Context String loggedInUser
    );

    @Mapping(target = "profile", expression = "java(profileServiceClient.getProfileById(tweet.getProfileId()))")
    @Mapping(target = "embeddedTweet", expression = "java(this.toResponse(tweet.getEmbeddedTweet(), retweetRepository, tweetRepository, profileServiceClient))")
    @Mapping(target = "parentTweetForReply", expression = "java(this.toResponse(tweet.getParentTweetForReply(), retweetRepository, tweetRepository, profileServiceClient))")
    @Mapping(target = "likes", expression = "java(tweet.getLikes().size())")
    @Mapping(target = "views", expression = "java(tweet.getViews().size())")
    @Mapping(target = "retweets", expression = "java(retweetRepository.countAllByParentTweetId(tweet.getId()))")
    @Mapping(target = "replies", expression = "java(tweetRepository.countAllByParentTweetForReplyId(tweet.getId()))")
    TweetResponse toResponse(
            Tweet tweet,
            @Context RetweetRepository retweetRepository,
            @Context TweetRepository tweetRepository,
            @Context ProfileServiceClient profileServiceClient
    );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileId", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "parentTweetForReply", ignore = true)
    @Mapping(target = "embeddedTweet", ignore = true)
    Tweet updateTweet(TweetUpdateRequest request, @MappingTarget Tweet tweet);
}
