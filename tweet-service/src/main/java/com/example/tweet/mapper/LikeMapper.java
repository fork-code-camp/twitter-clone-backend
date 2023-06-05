package com.example.tweet.mapper;

import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.entity.Like;
import com.example.tweet.entity.Tweet;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LikeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentTweet", expression = "java(parentTweet)")
    @Mapping(target = "profileId", expression = "java(profileServiceClient.getProfileIdByLoggedInUser(loggedInUser))")
    Like toEntity (
            Tweet parentTweet,
            @Context ProfileServiceClient profileServiceClient,
            @Context String loggedInUser
    );
}
