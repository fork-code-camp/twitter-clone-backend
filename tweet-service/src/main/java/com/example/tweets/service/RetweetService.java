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
    private final RetweetRepository retweetRepository;
    private final TweetService tweetService;
    private final ProfileServiceClient profileServiceClient;
    private final MessageSourceService messageSourceService;

    public boolean retweet(Long tweetId, String loggedInUser) {
        Tweet tweet = tweetService.getTweetEntityById(tweetId);

        return Optional.of(tweetId)
                .map(id -> retweetMapper.toEntity(tweet, profileServiceClient, loggedInUser))
                .map(retweetRepository::saveAndFlush)
                .isPresent();
    }

    public boolean undoRetweet(Long tweetId, String loggedInUser) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);

        retweetRepository.findByProfileIdAndTweetId(profileId, tweetId)
                .ifPresentOrElse(retweetRepository::delete, () -> {
                    throw new EntityNotFoundException(
                            messageSourceService.generateMessage("error.entity.not_found", tweetId)
                    );
                });
        return true;
    }

    public RetweetResponse getRetweetByUserAndTweetId(Long tweetId, String loggedInUser) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);

        return retweetRepository.findByProfileIdAndTweetId(profileId, tweetId)
                .map(retweet -> retweetMapper.toResponse(retweet, retweet.getTweet(), tweetMapper, profileServiceClient))
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", tweetId)
                ));
    }

    public List<RetweetResponse> getRetweetsForUser(String loggedInUser) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);

        return retweetRepository.findAllByProfileId(profileId)
                .stream()
                .map(retweet -> retweetMapper.toResponse(retweet, retweet.getTweet(), tweetMapper, profileServiceClient))
                .toList();
    }
}
