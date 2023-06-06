package com.example.tweet.mapper;

import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.entity.Tweet;
import com.example.tweet.entity.View;
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
