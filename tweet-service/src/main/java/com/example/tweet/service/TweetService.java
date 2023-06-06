package com.example.tweet.service;

import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.dto.request.TweetCreateRequest;
import com.example.tweet.dto.request.TweetUpdateRequest;
import com.example.tweet.dto.response.TweetResponse;
import com.example.tweet.entity.Tweet;
import com.example.tweet.exception.ActionNotAllowedException;
import com.example.tweet.exception.CreateEntityException;
import com.example.tweet.mapper.TweetMapper;
import com.example.tweet.repository.TweetRepository;
import com.example.tweet.util.TweetUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TweetService {

    private final TweetMapper tweetMapper;
    private final MessageSourceService messageSourceService;
    private final ProfileServiceClient profileServiceClient;
    private final TweetRepository tweetRepository;
    private final TweetUtil tweetUtil;
    private final ViewService viewService;

    public TweetResponse createTweet(TweetCreateRequest request, String loggedInUser) {
        return Optional.of(request)
                .map(req -> tweetMapper.toEntity(req, null, null, profileServiceClient, loggedInUser))
                .map(tweetRepository::saveAndFlush)
                .map(tweet -> tweetMapper.toResponse(tweet, tweetUtil, profileServiceClient))
                .orElseThrow(() -> new CreateEntityException(
                        messageSourceService.generateMessage("error.entity.unsuccessful_creation")
                ));
    }

    public TweetResponse createQuoteTweet(TweetCreateRequest request, Long tweetId, String loggedInUser) {
        return tweetRepository.findById(tweetId)
                .map(embeddedTweet -> tweetMapper.toEntity(request, embeddedTweet, null, profileServiceClient, loggedInUser))
                .map(tweetRepository::saveAndFlush)
                .map(quoteTweet -> tweetMapper.toResponse(quoteTweet, tweetUtil, profileServiceClient))
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", tweetId)
                ));
    }

    public TweetResponse getTweet(Long tweetId, String loggedInUser) {
        return tweetRepository.findById(tweetId)
                .map(tweet -> viewService.createViewEntity(tweet, loggedInUser, profileServiceClient))
                .map(tweet -> tweetMapper.toResponse(tweet, tweetUtil, profileServiceClient))
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", tweetId)
                ));
    }

    public List<TweetResponse> getAllTweets() {
        return tweetRepository.findAllByReplyToIsNull()
                .stream()
                .map(tweet -> tweetMapper.toResponse(tweet, tweetUtil, profileServiceClient))
                .toList();
    }

    public List<TweetResponse> getAllTweetsForUser(String loggedInUser) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);

        return tweetRepository.findAllByProfileIdAndReplyToIsNull(profileId)
                .stream()
                .map(tweet -> tweetMapper.toResponse(tweet, tweetUtil, profileServiceClient))
                .toList();
    }

    public TweetResponse updateTweet(Long id, TweetUpdateRequest request, String loggedInUser) {
        return tweetRepository.findById(id)
                .filter(tweet -> isTweetOwnedByLoggedInUser(tweet, loggedInUser))
                .map(tweet -> tweetMapper.updateTweet(request, tweet))
                .map(tweetRepository::saveAndFlush)
                .map(tweet -> tweetMapper.toResponse(tweet, tweetUtil, profileServiceClient))
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", id)
                ));
    }

    public Boolean deleteTweet(Long id, String loggedInUser) {
        return tweetRepository.findById(id)
                .filter(tweet -> isTweetOwnedByLoggedInUser(tweet, loggedInUser))
                .map(tweet -> {
                    tweetRepository.delete(tweet);
                    return tweet;
                })
                .isPresent();
    }

    public Tweet getTweetEntityById(Long tweetId) {
        return tweetRepository.findById(tweetId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", tweetId)
                ));
    }

    private boolean isTweetOwnedByLoggedInUser(Tweet tweet, String loggedInUser) {
        String profileIdOfLoggedInUser = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);

        if (!profileIdOfLoggedInUser.equals(tweet.getProfileId())) {
            throw new ActionNotAllowedException(
                    messageSourceService.generateMessage("error.action_not_allowed")
            );
        }

        return true;
    }
}
