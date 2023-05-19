package com.example.tweets.service;

import com.example.tweets.client.ProfileServiceClient;
import com.example.tweets.dto.request.TweetCreateRequest;
import com.example.tweets.dto.response.TweetResponse;
import com.example.tweets.entity.Tweet;
import com.example.tweets.exception.CreateEntityException;
import com.example.tweets.mapper.TweetMapper;
import com.example.tweets.repository.RetweetRepository;
import com.example.tweets.repository.TweetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final TweetService tweetService;
    private final RetweetRepository retweetRepository;
    private final TweetRepository tweetRepository;
    private final TweetMapper tweetMapper;
    private final ProfileServiceClient profileServiceClient;
    private final MessageSourceService messageSourceService;

    public TweetResponse reply(TweetCreateRequest tweetCreateRequest, Long parentTweetId, String loggedInUser) {
        Tweet parentTweet = tweetService.getTweetEntityById(parentTweetId);
        return Optional.of(tweetCreateRequest)
                .map(req -> tweetMapper.toEntity(req, null, parentTweet, profileServiceClient, loggedInUser))
                .map(tweetRepository::saveAndFlush)
                .map(reply -> tweetMapper.toResponse(reply, retweetRepository, tweetRepository, profileServiceClient))
                .orElseThrow(() -> new CreateEntityException(
                        messageSourceService.generateMessage("error.entity.unsuccessful_creation")
                ));
    }

    public List<TweetResponse> findAllRepliesForUser(String loggedInUser) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);
        return tweetRepository.findAllByProfileIdAndParentTweetForReplyIsNotNull(profileId)
                .stream()
                .map(reply -> tweetMapper.toResponse(reply, retweetRepository, tweetRepository, profileServiceClient))
                .toList();
    }

    public List<TweetResponse> findAllRepliesForTweet(Long parentTweetId) {
        return tweetRepository.findAllByParentTweetForReplyId(parentTweetId)
                .stream()
                .map(reply -> tweetMapper.toResponse(reply, retweetRepository, tweetRepository, profileServiceClient))
                .toList();
    }
}
