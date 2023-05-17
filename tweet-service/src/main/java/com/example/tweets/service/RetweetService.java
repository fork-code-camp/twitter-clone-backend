package com.example.tweets.service;

import com.example.tweets.client.ProfileServiceClient;
import com.example.tweets.dto.response.RetweetResponse;
import com.example.tweets.entity.Tweet;
import com.example.tweets.mapper.RetweetMapper;
import com.example.tweets.mapper.TweetMapper;
import com.example.tweets.repository.RetweetRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RetweetService {

    private final RetweetMapper retweetMapper;
    private final TweetMapper tweetMapper;
    private final TweetService tweetService;
    private final RetweetRepository retweetRepository;
    private final ProfileServiceClient profileServiceClient;
    private final MessageSourceService messageSourceService;

    public boolean retweet(Long tweetId, String loggedInUser) {
        Tweet parentTweet = tweetService.getTweetEntityById(tweetId);
        return Optional.of(parentTweet)
                .map(tweet -> retweetMapper.toEntity(tweet, profileServiceClient, loggedInUser))
                .map(retweetRepository::saveAndFlush)
                .isPresent();
    }

    public boolean undoRetweet(Long tweetId, String loggedInUser) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);
        retweetRepository.findByParentTweetIdAndProfileId(tweetId, profileId)
                .ifPresentOrElse(retweetRepository::delete, () -> {
                    throw new EntityNotFoundException(
                            messageSourceService.generateMessage("error.entity.not_found", tweetId)
                    );
                });
        return true;
    }

    public boolean isRetweeted(Long tweetId, String loggedInUser) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);
        return retweetRepository.findByParentTweetIdAndProfileId(tweetId, profileId).isPresent();
    }

    public RetweetResponse findRetweetById(Long retweetId) {
        return retweetRepository.findById(retweetId)
                .map(retweet -> retweetMapper.toResponse(retweet, tweetMapper, retweetRepository, profileServiceClient))
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", retweetId)
                ));
    }

    public List<RetweetResponse> findRetweetsForUser(String loggedInUser) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);
        return retweetRepository.findAllByProfileId(profileId)
                .stream()
                .map(retweet -> retweetMapper.toResponse(retweet, tweetMapper, retweetRepository, profileServiceClient))
                .toList();
    }
}
