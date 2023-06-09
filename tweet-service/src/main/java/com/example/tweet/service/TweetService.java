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
import com.example.tweet.util.MediaUtil;
import com.example.tweet.util.TweetUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static com.example.tweet.service.FanoutService.EntityCachePrefix.TWEETS;

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
    private final FanoutService fanoutService;

    public TweetResponse createTweet(TweetCreateRequest request, String loggedInUser, MultipartFile[] files) {
        return Optional.of(request)
                .map(req -> tweetMapper.toEntity(req, null, null, profileServiceClient, loggedInUser))
                .map(tweet -> mediaUtil.addMedia(tweet, files))
                .map(tweetRepository::saveAndFlush)
                .map(tweet -> tweetMapper.toResponse(tweet, tweetUtil, profileServiceClient))
                .map(tweet -> fanoutService.addToUserTimeline(tweet, TWEETS))
                .map(tweet -> fanoutService.addToHomeTimelines(tweet, TWEETS))
                .orElseThrow(() -> new CreateEntityException(
                        messageSourceService.generateMessage("error.entity.unsuccessful_creation")
                ));
    }

    public TweetResponse createQuoteTweet(TweetCreateRequest request, Long tweetId, String loggedInUser, MultipartFile[] files) {
        return tweetRepository.findById(tweetId)
                .map(quoteToTweet -> tweetMapper.toEntity(request, quoteToTweet, null, profileServiceClient, loggedInUser))
                .map(tweet -> mediaUtil.addMedia(tweet, files))
                .map(tweetRepository::saveAndFlush)
                .map(quoteTweet -> tweetMapper.toResponse(quoteTweet, tweetUtil, profileServiceClient))
                .map(tweet -> fanoutService.addToUserTimeline(tweet, TWEETS))
                .map(tweet -> fanoutService.addToHomeTimelines(tweet, TWEETS))
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
        return tweetRepository.findAllByProfileIdAndReplyToIsNullAndRetweetToIsNullOrderByCreationDateDesc(profileId)
                .stream()
                .map(tweet -> tweetMapper.toResponse(tweet, tweetUtil, profileServiceClient))
                .toList();
    }

    @CachePut(cacheNames = "tweets", key = "#p0")
    public TweetResponse updateTweet(Long tweetId, TweetUpdateRequest request, String loggedInUser, MultipartFile[] files) {
        return tweetRepository.findById(tweetId)
                .filter(tweet -> isTweetOwnedByLoggedInUser(tweet, loggedInUser))
                .map(tweet -> tweetMapper.updateTweet(request, tweet))
                .map(tweet -> mediaUtil.updateMedia(tweet, files))
                .map(tweetRepository::saveAndFlush)
                .map(tweet -> tweetMapper.toResponse(tweet, tweetUtil, profileServiceClient))
                .map(tweet -> fanoutService.updateInUserTimeline(tweet, TWEETS))
                .map(tweet -> fanoutService.updateInHomeTimelines(tweet, TWEETS))
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", tweetId)
                ));
    }

    @CacheEvict(cacheNames = "tweets", key = "#p0")
    public Boolean deleteTweet(Long tweetId, String loggedInUser) {
        return tweetRepository.findById(tweetId)
                .filter(tweet -> isTweetOwnedByLoggedInUser(tweet, loggedInUser))
                .map(tweet -> {
                    fanoutService.deleteFromUserTimeline(tweet, TWEETS);
                    fanoutService.deleteFromHomeTimelines(tweet, TWEETS);
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
