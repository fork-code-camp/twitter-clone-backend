package com.example.tweet.service;

import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.dto.response.TweetResponse;
import com.example.tweet.entity.Tweet;
import com.example.tweet.mapper.TweetMapper;
import com.example.tweet.repository.TweetRepository;
import com.example.tweet.util.TweetUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.tweet.service.FanoutService.EntityName.RETWEETS;

@Service
@RequiredArgsConstructor
public class RetweetService {

    private final TweetMapper tweetMapper;
    private final TweetService tweetService;
    private final TweetUtil tweetUtil;
    private final TweetRepository tweetRepository;
    private final ProfileServiceClient profileServiceClient;
    private final MessageSourceService messageSourceService;
    private final FanoutService fanoutService;

    public boolean retweet(Long retweetToId, String loggedInUser) {
        Tweet retweetTo = tweetService.getTweetEntityById(retweetToId);
        return Optional.of(retweetTo)
                .map(tweet -> tweetMapper.toEntity(tweet, profileServiceClient, loggedInUser))
                .map(tweetRepository::saveAndFlush)
                .map(retweet -> tweetMapper.toResponse(retweet, tweetUtil, profileServiceClient))
                .map(retweet -> fanoutService.addToUserTimeline(retweet, RETWEETS))
                .map(retweet -> fanoutService.addToHomeTimelines(retweet, RETWEETS))
                .isPresent();
    }

    @CacheEvict(cacheNames = "retweets", key = "#p0")
    public boolean undoRetweet(Long retweetId) {
        tweetRepository.findByIdAndRetweetToIsNotNull(retweetId)
                .ifPresentOrElse(retweet -> {
                    fanoutService.deleteFromUserTimeline(retweet, RETWEETS);
                    fanoutService.deleteFromHomeTimelines(retweet, RETWEETS);
                    tweetRepository.delete(retweet);
                }, () -> {
                    throw new EntityNotFoundException(
                            messageSourceService.generateMessage("error.entity.not_found", retweetId)
                    );
                });
        return true;
    }

    @Cacheable(
            cacheNames = "retweets",
            key = "#p0",
            unless = "#result.retweetTo.likes < 5000 && #result.retweetTo.views < 25000 && #result.retweetTo.replies < 1000 && #result.retweetTo.retweets < 1000"
    )
    public TweetResponse findRetweetById(Long retweetId) {
        return tweetRepository.findByIdAndRetweetToIsNotNull(retweetId)
                .map(retweet -> tweetMapper.toResponse(retweet, tweetUtil, profileServiceClient))
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", retweetId)
                ));
    }

    public List<TweetResponse> findRetweetsForUser(String loggedInUser, PageRequest page) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);
        return tweetRepository.findAllByProfileIdAndRetweetToIsNotNullOrderByCreationDateDesc(profileId, page)
                .stream()
                .map(retweet -> tweetMapper.toResponse(retweet, tweetUtil, profileServiceClient))
                .collect(Collectors.toList());
    }
}
