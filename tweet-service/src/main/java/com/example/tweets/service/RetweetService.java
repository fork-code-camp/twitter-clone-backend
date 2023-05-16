package com.example.tweets.service;

import com.example.tweets.client.ProfileServiceClient;
import com.example.tweets.dto.request.RetweetRequest;
import com.example.tweets.dto.response.TweetResponse;
import com.example.tweets.entity.Tweet;
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

    private final TweetMapper tweetMapper;
    private final RetweetRepository retweetRepository;
    private final TweetService tweetService;
    private final ProfileServiceClient profileServiceClient;
    private final MessageSourceService messageSourceService;

    public boolean retweet(RetweetRequest retweetRequest, Long tweetId, String loggedInUser) {
        Tweet originalTweet = tweetService.getTweetEntityById(tweetId);

        return Optional.of(retweetRequest)
                .map(req -> tweetMapper.toEntity(retweetRequest, originalTweet, profileServiceClient, loggedInUser))
                .map(retweetRepository::saveAndFlush)
                .isPresent();
    }

    public boolean undoRetweet(Long tweetId, String loggedInUser) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);
        retweetRepository.findByOriginalTweetIdAndProfileId(tweetId, profileId)
                .ifPresentOrElse(retweetRepository::delete, () -> {
                    throw new EntityNotFoundException(
                            messageSourceService.generateMessage("error.entity.not_found", tweetId)
                    );
                });
        return true;
    }

    public TweetResponse getRetweetByOriginalTweetId(Long tweetId, String loggedInUser) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);
        return retweetRepository.findByOriginalTweetIdAndProfileId(tweetId, profileId)
                .map(retweet -> tweetMapper.toResponse(retweet, retweetRepository, profileServiceClient))
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", tweetId)
                ));
    }

    public List<TweetResponse> getRetweetsForUser(String loggedInUser) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);
        return retweetRepository.findAllByProfileIdAndOriginalTweetNotNull(profileId)
                .stream()
                .map(retweet -> tweetMapper.toResponse(retweet, retweetRepository, profileServiceClient))
                .toList();
    }
}
