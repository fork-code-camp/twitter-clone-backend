package com.example.tweets.mapper;

import com.example.tweets.client.ProfileServiceClient;
import com.example.tweets.entity.Tweet;
import com.example.tweets.entity.View;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ViewMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileId", expression = "java(profileServiceClient.getProfileIdByLoggedInUser(loggedInUser))")
    @Mapping(target = "parentTweet", expression = "java(parentTweet)")
    View toEntity(
            Tweet parentTweet,
            @Context String loggedInUser,
            @Context ProfileServiceClient profileServiceClient
    );
}
