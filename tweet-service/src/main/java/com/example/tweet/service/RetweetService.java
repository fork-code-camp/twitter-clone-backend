package com.example.tweet.service;

import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.dto.response.RetweetResponse;
import com.example.tweet.entity.Tweet;
import com.example.tweet.mapper.RetweetMapper;
import com.example.tweet.mapper.TweetMapper;
import com.example.tweet.repository.RetweetRepository;
import com.example.tweet.util.TweetUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RetweetService {

    private final RetweetMapper retweetMapper;
    private final TweetMapper tweetMapper;
    private final TweetService tweetService;
    private final TweetUtil tweetUtil;
    private final RetweetRepository retweetRepository;
    private final ProfileServiceClient profileServiceClient;
    private final MessageSourceService messageSourceService;
    private final FanoutService.UserTimelineService userTimelineService;
    private final FanoutService.HomeTimelineService homeTimelineService;

    public boolean retweet(Long parentTweetId, String loggedInUser) {
        Tweet parentTweet = tweetService.getTweetEntityById(parentTweetId);
        return Optional.of(parentTweet)
                .map(tweet -> retweetMapper.toEntity(tweet, profileServiceClient, loggedInUser))
                .map(retweetRepository::saveAndFlush)
                .map(retweet -> retweetMapper.toResponse(retweet, tweetMapper, tweetUtil, profileServiceClient))
                .map(userTimelineService::addRetweetToUserTimeline)
                .map(homeTimelineService::addRetweetToHomeTimelines)
                .isPresent();
    }

    @CacheEvict(cacheNames = "retweets", key = "#p0")
    public boolean undoRetweet(Long parentTweetId, String loggedInUser) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);
        retweetRepository.findByParentTweetIdAndProfileId(parentTweetId, profileId)
                .ifPresentOrElse(retweet -> {
                    userTimelineService.deleteRetweetFromUserTimeline(retweet);
                    homeTimelineService.deleteRetweetFromHomeTimelines(retweet);
                    retweetRepository.delete(retweet);
                }, () -> {
                    throw new EntityNotFoundException(
                            messageSourceService.generateMessage("error.entity.not_found", parentTweetId)
                    );
                });
        return true;
    }

    @Cacheable(
            cacheNames = "retweets",
            key = "#p0",
            unless = "#result.parentTweet.likes < 5000 && #result.parentTweet.views < 25000 && #result.parentTweet.replies < 1000 && #result.parentTweet.retweets < 1000"
    )
    public RetweetResponse findRetweetById(Long retweetId) {
        return retweetRepository.findById(retweetId)
                .map(retweet -> retweetMapper.toResponse(retweet, tweetMapper, tweetUtil, profileServiceClient))
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", retweetId)
                ));
    }

    public List<RetweetResponse> findRetweetsForUser(String loggedInUser) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);
        return retweetRepository.findAllByProfileIdOrderByRetweetTimeDesc(profileId)
                .stream()
                .map(retweet -> retweetMapper.toResponse(retweet, tweetMapper, tweetUtil, profileServiceClient))
                .toList();
    }
}
