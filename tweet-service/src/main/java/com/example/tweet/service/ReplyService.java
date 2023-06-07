package com.example.tweet.service;

import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.dto.request.TweetCreateRequest;
import com.example.tweet.dto.response.TweetResponse;
import com.example.tweet.entity.Tweet;
import com.example.tweet.exception.CreateEntityException;
import com.example.tweet.mapper.TweetMapper;
import com.example.tweet.repository.TweetRepository;
import com.example.tweet.util.TweetUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final TweetService tweetService;
    private final TweetRepository tweetRepository;
    private final TweetUtil tweetUtil;
    private final TweetMapper tweetMapper;
    private final ProfileServiceClient profileServiceClient;
    private final MessageSourceService messageSourceService;
    private final FanoutService.UserTimelineService userTimelineService;

    public TweetResponse reply(TweetCreateRequest tweetCreateRequest, Long replyToId, String loggedInUser) {
        Tweet parentTweet = tweetService.getTweetEntityById(replyToId);
        return Optional.of(tweetCreateRequest)
                .map(req -> tweetMapper.toEntity(req, null, parentTweet, profileServiceClient, loggedInUser))
                .map(reply -> {
                    parentTweet.getReplies().add(reply);
                    return reply;
                })
                .map(tweetRepository::saveAndFlush)
                .map(reply -> tweetMapper.toResponse(reply, tweetUtil, profileServiceClient))
                .map(userTimelineService::addTweetToUserTimeline)
                .orElseThrow(() -> new CreateEntityException(
                        messageSourceService.generateMessage("error.entity.unsuccessful_creation")
                ));
    }

    public List<TweetResponse> findAllRepliesForUser(String loggedInUser) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);
        return tweetRepository.findAllByProfileIdAndReplyToIsNotNullOrderByCreationDateDesc(profileId)
                .stream()
                .map(reply -> tweetMapper.toResponse(reply, tweetUtil, profileServiceClient))
                .collect(Collectors.toList());
    }

    @Cacheable(cacheNames = "repliesForTweet", unless = "#result.size() < 1000")
    public List<TweetResponse> findAllRepliesForTweet(Long replyToId) {
        return tweetRepository.findAllByReplyToId(replyToId)
                .stream()
                .map(reply -> tweetMapper.toResponse(reply, tweetUtil, profileServiceClient))
                .collect(Collectors.toList());
    }
}