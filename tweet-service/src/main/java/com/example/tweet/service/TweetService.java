package com.example.tweet.service;

import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.dto.request.TweetCreateRequest;
import com.example.tweet.dto.request.TweetUpdateRequest;
import com.example.tweet.dto.response.ProfileResponse;
import com.example.tweet.dto.response.TweetResponse;
import com.example.tweet.exception.CreateEntityException;
import com.example.tweet.mapper.TweetMapper;
import com.example.tweet.repository.TweetRepository;
import com.example.tweet.util.MediaUtil;
import com.example.tweet.util.TweetUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static com.example.tweet.constants.CacheName.TWEETS_CACHE_NAME;
import static com.example.tweet.constants.EntityName.TWEETS;
import static com.example.tweet.constants.Operation.*;
import static com.example.tweet.constants.TopicName.HOME_TIMELINE_TOPIC;
import static com.example.tweet.constants.TopicName.USER_TIMELINE_TOPIC;

@Service
@RequiredArgsConstructor
public class TweetService {

    private final TweetMapper tweetMapper;
    private final MessageSourceService messageSourceService;
    private final ProfileServiceClient profileServiceClient;
    private final TweetRepository tweetRepository;
    private final TweetUtil tweetUtil;
    private final MediaUtil mediaUtil;
    private final ViewService viewService;

    public TweetResponse createTweet(TweetCreateRequest request, String loggedInUser, MultipartFile[] files) {
        return Optional.of(request)
                .map(req -> tweetMapper.toEntity(req, null, null, profileServiceClient, loggedInUser))
                .map(tweet -> mediaUtil.addMedia(tweet, files))
                .map(tweetRepository::saveAndFlush)
                .map(tweet -> tweetMapper.toResponse(tweet, loggedInUser, tweetUtil, profileServiceClient))
                .map(tweet -> tweetUtil.sendMessageToKafka(USER_TIMELINE_TOPIC, tweet, TWEETS, ADD))
                .map(tweet -> tweetUtil.sendMessageToKafka(HOME_TIMELINE_TOPIC, tweet, TWEETS, ADD))
                .orElseThrow(() -> new CreateEntityException(
                        messageSourceService.generateMessage("error.entity.unsuccessful_creation")
                ));
    }

    public TweetResponse createQuoteTweet(TweetCreateRequest request, Long tweetId, String loggedInUser, MultipartFile[] files) {
        return tweetRepository.findById(tweetId)
                .map(quoteToTweet -> tweetMapper.toEntity(request, quoteToTweet, null, profileServiceClient, loggedInUser))
                .map(tweet -> mediaUtil.addMedia(tweet, files))
                .map(tweetRepository::saveAndFlush)
                .map(quoteTweet -> tweetMapper.toResponse(quoteTweet, loggedInUser, tweetUtil, profileServiceClient))
                .map(tweet -> tweetUtil.sendMessageToKafka(USER_TIMELINE_TOPIC, tweet, TWEETS, ADD))
                .map(tweet -> tweetUtil.sendMessageToKafka(HOME_TIMELINE_TOPIC, tweet, TWEETS, ADD))
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", tweetId)
                ));
    }

    @Cacheable(
            cacheNames = TWEETS_CACHE_NAME,
            key = "#p0",
            unless = "#result.likes < 5000 && #result.views < 25000 && #result.replies < 1000 && #result.retweets < 1000"
    )
    public TweetResponse getTweet(Long tweetId, String loggedInUser) {
        return tweetRepository.findById(tweetId)
                .map(tweet -> viewService.createViewEntity(tweet, loggedInUser, profileServiceClient))
                .map(tweet -> tweetMapper.toResponse(tweet, loggedInUser, tweetUtil, profileServiceClient))
                .map(tweet -> tweetUtil.sendMessageToKafka(USER_TIMELINE_TOPIC, tweet, TWEETS, UPDATE))
                .map(tweet ->  tweetUtil.sendMessageToKafka(HOME_TIMELINE_TOPIC, tweet, TWEETS, UPDATE))
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", tweetId)
                ));
    }

    public List<TweetResponse> getAllTweetsForUser(String profileId, PageRequest page) {
        ProfileResponse profile = profileServiceClient.getProfileById(profileId);
        return tweetRepository.findAllByProfileIdAndReplyToIsNullAndRetweetToIsNullOrderByCreationDateDesc(profileId, page)
                .stream()
                .map(tweet -> tweetMapper.toResponse(tweet, profile.getEmail(), tweetUtil, profileServiceClient))
                .toList();
    }

    @CachePut(cacheNames = TWEETS_CACHE_NAME, key = "#p0")
    public TweetResponse updateTweet(Long tweetId, TweetUpdateRequest request, String loggedInUser, MultipartFile[] files) {
        return tweetRepository.findById(tweetId)
                .filter(tweet -> tweetUtil.isTweetOwnedByLoggedInUser(tweet, loggedInUser, profileServiceClient, messageSourceService))
                .map(tweet -> tweetMapper.updateTweet(request, tweet))
                .map(tweet -> mediaUtil.updateMedia(tweet, files))
                .map(tweetRepository::saveAndFlush)
                .map(tweet -> tweetMapper.toResponse(tweet, loggedInUser, tweetUtil, profileServiceClient))
                .map(tweet -> tweetUtil.sendMessageToKafka(USER_TIMELINE_TOPIC, tweet, TWEETS, UPDATE))
                .map(tweet -> tweetUtil.sendMessageToKafka(HOME_TIMELINE_TOPIC, tweet, TWEETS, UPDATE))
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", tweetId)
                ));
    }

    @CacheEvict(cacheNames = TWEETS_CACHE_NAME, key = "#p0")
    public Boolean deleteTweet(Long tweetId, String loggedInUser) {
        return tweetRepository.findById(tweetId)
                .filter(tweet -> tweetUtil.isTweetOwnedByLoggedInUser(tweet, loggedInUser, profileServiceClient, messageSourceService))
                .map(tweet -> {
                    TweetResponse response = tweetMapper.toResponse(tweet, loggedInUser, tweetUtil, profileServiceClient);
                    tweetUtil.sendMessageToKafka(USER_TIMELINE_TOPIC, response, TWEETS, DELETE);
                    tweetUtil.sendMessageToKafka(HOME_TIMELINE_TOPIC, response, TWEETS, DELETE);
                    tweetRepository.delete(tweet);
                    return tweet;
                })
                .isPresent();
    }
}
