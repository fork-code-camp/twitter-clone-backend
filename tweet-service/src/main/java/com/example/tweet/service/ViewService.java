package com.example.tweet.service;

import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.entity.Tweet;
import com.example.tweet.exception.CreateEntityException;
import com.example.tweet.mapper.ViewMapper;
import com.example.tweet.repository.ViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ViewService {

    private final ViewRepository viewRepository;
    private final ViewMapper viewMapper;
    private final MessageSourceService messageSourceService;


    public Tweet createViewEntity(Tweet parentTweet, String loggedInUser, ProfileServiceClient profileServiceClient) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);
        if (viewRepository.findByProfileIdAndParentTweetId(profileId, parentTweet.getId()).isEmpty()) {
            Optional.of(parentTweet)
                    .map(tweet -> viewMapper.toEntity(tweet, loggedInUser, profileServiceClient))
                    .map(viewRepository::saveAndFlush)
                    .orElseThrow(() -> new CreateEntityException(
                            messageSourceService.generateMessage("error.entity.unsuccessful_creation")
                    ));
        }
        return parentTweet;
    }
}
