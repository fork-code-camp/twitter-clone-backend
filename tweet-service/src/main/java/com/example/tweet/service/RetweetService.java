package com.example.tweet.service;

import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.dto.response.ProfileResponse;
import com.example.tweet.dto.response.TweetResponse;
import com.example.tweet.mapper.TweetMapper;
import com.example.tweet.repository.TweetRepository;
import com.example.tweet.util.TweetUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.example.tweet.constants.CacheName.RETWEETS_CACHE_NAME;
import static com.example.tweet.constants.EntityName.RETWEETS;
import static com.example.tweet.constants.EntityName.TWEETS;
import static com.example.tweet.constants.Operation.*;
import static com.example.tweet.constants.TopicName.HOME_TIMELINE_TOPIC;
import static com.example.tweet.constants.TopicName.USER_TIMELINE_TOPIC;

@Service
@RequiredArgsConstructor
public class RetweetService {

    private final TweetMapper tweetMapper;
    private final TweetUtil tweetUtil;
    private final TweetRepository tweetRepository;
    private final ProfileServiceClient profileServiceClient;
    private final MessageSourceService messageSourceService;
    private final CacheManager cacheManager;

    public boolean retweet(Long retweetToId, String loggedInUser) {
        tweetRepository.findById(retweetToId)
                .map(tweet -> tweetMapper.toEntity(tweet, profileServiceClient, loggedInUser))
                .map(tweetRepository::saveAndFlush)
                .map(retweet -> tweetMapper.toResponse(retweet, loggedInUser, tweetUtil, profileServiceClient))
                .map(retweet -> {
                    tweetUtil.sendMessageToKafka(USER_TIMELINE_TOPIC, retweet, RETWEETS, ADD);
                    tweetUtil.sendMessageToKafka(HOME_TIMELINE_TOPIC, retweet, RETWEETS, ADD);
                    tweetUtil.sendMessageToKafka(USER_TIMELINE_TOPIC, retweet.getRetweetTo(), TWEETS, UPDATE);
                    tweetUtil.sendMessageToKafka(HOME_TIMELINE_TOPIC, retweet.getRetweetTo(), TWEETS, UPDATE);
                    return retweet;
                })
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", retweetToId)
                ));
        return true;
    }

    public boolean undoRetweet(Long retweetToId, String loggedInUser) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);
        tweetRepository.findByRetweetToIdAndProfileId(retweetToId, profileId)
                .ifPresentOrElse(retweet -> {
                    Objects.requireNonNull(cacheManager.getCache(RETWEETS_CACHE_NAME)).evictIfPresent(Long.toString(retweet.getId()));
                    tweetRepository.delete(retweet);
                    TweetResponse retweetResponse = tweetMapper.toResponse(retweet, loggedInUser, tweetUtil, profileServiceClient);
                    tweetUtil.sendMessageToKafka(USER_TIMELINE_TOPIC, retweetResponse, RETWEETS, DELETE);
                    tweetUtil.sendMessageToKafka(HOME_TIMELINE_TOPIC, retweetResponse, RETWEETS, DELETE);
                    tweetUtil.sendMessageToKafka(HOME_TIMELINE_TOPIC, retweetResponse.getRetweetTo(), TWEETS, UPDATE);
                    tweetUtil.sendMessageToKafka(HOME_TIMELINE_TOPIC, retweetResponse.getRetweetTo(), TWEETS, UPDATE);
                }, () -> {
                    throw new EntityNotFoundException(
                            messageSourceService.generateMessage("error.entity.not_found", retweetToId)
                    );
                });
        return true;
    }

    @Cacheable(
            cacheNames = RETWEETS_CACHE_NAME,
            key = "#p0",
            unless = "#result.retweetTo.likes < 5000 && #result.retweetTo.views < 25000 && #result.retweetTo.replies < 1000 && #result.retweetTo.retweets < 1000"
    )
    public TweetResponse getRetweetById(Long retweetId, String loggedInUser) {
        return tweetRepository.findByIdAndRetweetToIsNotNull(retweetId)
                .map(retweet -> tweetMapper.toResponse(retweet, loggedInUser, tweetUtil, profileServiceClient))
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", retweetId)
                ));
    }

    public List<TweetResponse> getAllRetweetsForUser(String profileId, PageRequest page) {
        ProfileResponse profile = profileServiceClient.getProfileById(profileId);
        return tweetRepository.findAllByProfileIdAndRetweetToIsNotNullOrderByCreationDateDesc(profileId, page)
                .stream()
                .map(retweet -> tweetMapper.toResponse(retweet, profile.getEmail(), tweetUtil, profileServiceClient))
                .collect(Collectors.toList());
    }
}
