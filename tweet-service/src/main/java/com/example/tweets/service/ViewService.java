package com.example.tweets.service;

import com.example.tweets.client.ProfileServiceClient;
import com.example.tweets.entity.Tweet;
import com.example.tweets.exception.CreateEntityException;
import com.example.tweets.mapper.ViewMapper;
import com.example.tweets.repository.ViewRepository;
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
