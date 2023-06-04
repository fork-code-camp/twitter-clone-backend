package com.example.tweets.service;

import com.example.tweets.client.ProfileServiceClient;
import com.example.tweets.dto.request.TweetCreateRequest;
import com.example.tweets.dto.request.TweetUpdateRequest;
import com.example.tweets.dto.response.TweetResponse;
import com.example.tweets.entity.Tweet;
import com.example.tweets.exception.ActionNotAllowedException;
import com.example.tweets.exception.CreateEntityException;
import com.example.tweets.mapper.TweetMapper;
import com.example.tweets.repository.TweetRepository;
import com.example.tweets.util.TweetUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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
    private final FanoutService.UserTimelineService userTimelineService;
    private final FanoutService.HomeTimelineService homeTimelineService;

    public TweetResponse createTweet(TweetCreateRequest request, String loggedInUser) {
        return Optional.of(request)
                .map(req -> tweetMapper.toEntity(req, null, null, profileServiceClient, loggedInUser))
                .map(tweetRepository::saveAndFlush)
                .map(tweet -> tweetMapper.toResponse(tweet, tweetUtil, profileServiceClient))
                .map(userTimelineService::addTweetToUserTimeline)
                .map(homeTimelineService::addTweetToHomeTimelines)
                .orElseThrow(() -> new CreateEntityException(
                        messageSourceService.generateMessage("error.entity.unsuccessful_creation")
                ));
    }

    public TweetResponse createQuoteTweet(TweetCreateRequest request, Long tweetId, String loggedInUser) {
        return tweetRepository.findById(tweetId)
                .map(quoteToTweet -> tweetMapper.toEntity(request, quoteToTweet, null, profileServiceClient, loggedInUser))
                .map(tweetRepository::saveAndFlush)
                .map(quoteTweet -> tweetMapper.toResponse(quoteTweet, tweetUtil, profileServiceClient))
                .map(userTimelineService::addTweetToUserTimeline)
                .map(homeTimelineService::addTweetToHomeTimelines)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", tweetId)
                ));
    }

    @Cacheable(
            cacheNames = "tweets",
            key = "#p0",
            unless = "#result.likes < 5000 && #result.views < 25000 && #result.replies < 1000 && #result.retweets < 1000"
    )
    public TweetResponse getTweet(Long tweetId, String loggedInUser) {
        return tweetRepository.findById(tweetId)
                .map(tweet -> viewService.createViewEntity(tweet, loggedInUser, profileServiceClient))
                .map(tweet -> tweetMapper.toResponse(tweet, tweetUtil, profileServiceClient))
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", tweetId)
                ));
    }

    public List<TweetResponse> getAllTweetsForUser(String loggedInUser) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);
        return tweetRepository.findAllByProfileIdAndReplyToIsNullOrderByCreationDateDesc(profileId)
                .stream()
                .map(tweet -> tweetMapper.toResponse(tweet, tweetUtil, profileServiceClient))
                .toList();
    }

    @CachePut(cacheNames = "tweets", key = "#p0")
    public TweetResponse updateTweet(Long tweetId, TweetUpdateRequest request, String loggedInUser) {
        return tweetRepository.findById(tweetId)
                .filter(tweet -> isTweetOwnedByLoggedInUser(tweet, loggedInUser))
                .map(tweet -> tweetMapper.updateTweet(request, tweet))
                .map(tweetRepository::saveAndFlush)
                .map(tweet -> tweetMapper.toResponse(tweet, tweetUtil, profileServiceClient))
                .map(userTimelineService::updateTweetInUserTimeline)
                .map(homeTimelineService::updateTweetInHomeTimelines)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", tweetId)
                ));
    }

    @CacheEvict(cacheNames = "tweets", key = "#p0")
    public Boolean deleteTweet(Long tweetId, String loggedInUser) {
        return tweetRepository.findById(tweetId)
                .filter(tweet -> isTweetOwnedByLoggedInUser(tweet, loggedInUser))
                .map(tweet -> {
                    userTimelineService.deleteTweetFromUserTimeline(tweet);
                    homeTimelineService.deleteTweetFromHomeTimelines(tweet);
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
