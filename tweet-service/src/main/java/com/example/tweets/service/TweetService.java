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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TweetService {

    private final TweetRepository tweetRepository;
    private final TweetMapper tweetMapper;
    private final MessageSourceService messageSourceService;
    private final ProfileServiceClient profileServiceClient;

    public TweetResponse createTweet(TweetCreateRequest request, String loggedInUser) {
        return Optional.of(request)
                .map(req -> tweetMapper.toEntity(req, profileServiceClient, loggedInUser))
                .map(tweetRepository::saveAndFlush)
                .map(tweet -> tweetMapper.toResponse(tweet, profileServiceClient))
                .orElseThrow(() -> new CreateEntityException(
                        messageSourceService.generateMessage("error.entity.unsuccessful_creation")
                ));
    }

    public TweetResponse getTweet(Long id) {
        return tweetRepository.findById(id)
                .map(tweet -> tweetMapper.toResponse(tweet, profileServiceClient))
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", id)
                ));
    }

    public List<TweetResponse> getAllTweets() {
        return tweetRepository.findAll()
                .stream()
                .map(tweet -> tweetMapper.toResponse(tweet, profileServiceClient))
                .toList();
    }

    public List<TweetResponse> getAllTweetsForUser(String loggedInUser) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);

        return tweetRepository.findAllByProfileId(profileId)
                .stream()
                .map(tweet -> tweetMapper.toResponse(tweet, profileServiceClient))
                .toList();
    }

    public TweetResponse updateTweet(Long id, TweetUpdateRequest request, String loggedInUser) {
        return tweetRepository.findById(id)
                .filter(tweet -> isTweetOwnedByLoggedInUser(tweet, loggedInUser))
                .map(tweet -> tweetMapper.updateTweet(request, tweet))
                .map(tweetRepository::saveAndFlush)
                .map(tweet -> tweetMapper.toResponse(tweet, profileServiceClient))
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
