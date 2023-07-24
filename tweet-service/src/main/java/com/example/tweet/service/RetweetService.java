package com.example.tweet.service;

import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.dto.response.ProfileResponse;
import com.example.tweet.dto.response.TweetResponse;
import com.example.tweet.mapper.TweetMapper;
import com.example.tweet.repository.TweetRepository;
import com.example.tweet.util.TweetUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.example.tweet.constant.CacheName.RETWEETS_CACHE_NAME;
import static com.example.tweet.constant.CacheName.TWEETS_CACHE_NAME;
import static com.example.tweet.constant.Operation.ADD;
import static com.example.tweet.constant.Operation.DELETE;

@Service
@RequiredArgsConstructor
public class RetweetService {

    private final TweetMapper tweetMapper;
    private final TweetUtil tweetUtil;
    private final TweetRepository tweetRepository;
    private final ProfileServiceClient profileServiceClient;
    private final MessageSourceService messageSourceService;
    private final CacheManager cacheManager;
    private final TweetService tweetService;

    public boolean retweet(Long retweetToId, String loggedInUser) {
        tweetRepository.findById(retweetToId)
                .map(tweet -> tweetMapper.toEntity(tweet, profileServiceClient, loggedInUser))
                .map(tweetRepository::saveAndFlush)
                .map(retweet -> {
                    tweetUtil.sendMessageWithRetweet(retweet, ADD);
                    tweetUtil.evictEntityFromCache(retweetToId, TWEETS_CACHE_NAME);
                    return tweetMapper.toResponse(retweet, loggedInUser, tweetUtil, profileServiceClient);
                })
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", retweetToId)
                ));
        return true;
    }

    public boolean undoRetweet(Long retweetToId, String loggedInUser) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);
        tweetRepository.findByRetweetToIdAndProfileId(retweetToId, profileId)
                .filter(retweet -> tweetUtil.isEntityOwnedByLoggedInUser(retweet, loggedInUser))
                .ifPresentOrElse(retweet -> {
                    tweetUtil.evictEntityFromCache(retweet.getId(), RETWEETS_CACHE_NAME);
                    tweetUtil.evictEntityFromCache(retweetToId, TWEETS_CACHE_NAME);
                    tweetUtil.sendMessageWithRetweet(retweet, DELETE);
                    tweetRepository.delete(retweet);
                }, () -> {
                    throw new EntityNotFoundException(
                            messageSourceService.generateMessage("error.entity.not_found", retweetToId)
                    );
                });
        return true;
    }

    public TweetResponse getRetweetById(Long retweetId, String loggedInUser) {
        Cache cache = Objects.requireNonNull(cacheManager.getCache(RETWEETS_CACHE_NAME));
        TweetResponse retweetResponse = cache.get(retweetId, TweetResponse.class);
        if (retweetResponse != null) {
            return updateRetweetResponse(tweetUtil.updateProfileInResponse(retweetResponse));
        }
        return tweetRepository.findByIdAndRetweetToIsNotNull(retweetId)
                .map(retweet -> {
                    TweetResponse response = tweetMapper.toResponse(retweet, loggedInUser, tweetUtil, profileServiceClient);
                    cache.put(retweetId, response);
                    return response;
                })
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

    private TweetResponse updateRetweetResponse(TweetResponse retweetResponse) {
        retweetResponse.setRetweetTo(tweetService.getTweetById(
                retweetResponse.getRetweetTo().getId(),
                retweetResponse.getProfile().getEmail()
        ));
        return retweetResponse;
    }
}
