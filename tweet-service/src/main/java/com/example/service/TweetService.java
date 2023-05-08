package com.example.service;

import com.example.client.ProfileClient;
import com.example.dto.request.TweetCreateRequest;
import com.example.dto.request.TweetUpdateRequest;
import com.example.dto.response.TweetResponse;
import com.example.entity.Tweet;
import com.example.exception.CreateEntityException;
import com.example.mapper.TweetMapper;
import com.example.repository.TweetRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TweetService {

    private final TweetRepository tweetRepository;
    private final TweetMapper tweetMapper;
    private final MessageSourceService messageSourceService;
    private final ProfileClient profileClient;

    public TweetResponse createTweet(TweetCreateRequest request, String loggedInUser) {
        return Optional.of(request)
                .map(req -> tweetMapper.toEntity(req, profileClient, loggedInUser))
                .map(tweetRepository::saveAndFlush)
                .map(tweet -> tweetMapper.toResponse(tweet, profileClient))
                .orElseThrow(() -> new CreateEntityException(
                        messageSourceService.generateMessage("error.unsuccessful_creation")
                ));
    }

    public TweetResponse getTweet(Long id) {
        return tweetRepository.findById(id)
                .map(tweet -> tweetMapper.toResponse(tweet, profileClient))
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", id)
                ));
    }

    public TweetResponse updateTweet(Long id, TweetUpdateRequest request, String loggedInUser) {
        return tweetRepository.findById(id)
                .filter(tweet -> isTweetOwnedByLoggedInUser(tweet, loggedInUser))
                .map(tweet -> tweetMapper.updateTweet(request, tweet))
                .map(tweetRepository::saveAndFlush)
                .map(tweet -> tweetMapper.toResponse(tweet, profileClient))
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

    private boolean isTweetOwnedByLoggedInUser(Tweet tweet, String loggedInUser) {
        return profileClient.getProfileIdByLoggedInUser(loggedInUser).equals(tweet.getProfileId());
    }
}
