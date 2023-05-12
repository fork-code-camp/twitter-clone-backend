package com.example.tweets.service;

import com.example.tweets.client.ProfileServiceClient;
import com.example.tweets.exception.CreateEntityException;
import com.example.tweets.mapper.LikeMapper;
import com.example.tweets.repository.LikesRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikesRepository likesRepository;
    private final TweetService tweetService;
    private final ProfileServiceClient profileServiceClient;
    private final LikeMapper likeMapper;
    private final MessageSourceService messageSourceService;

    public void likeTweet(Long tweetId, String loggedInUser) {
        Optional.of(tweetId)
                .map(id -> likeMapper.toEntityFromTweetId(id, tweetService, profileServiceClient, loggedInUser))
                .map(likesRepository::saveAndFlush)
                .orElseThrow(() -> new CreateEntityException(
                        messageSourceService.generateMessage("error.entity.unsuccessful_creation")
                ));
    }

    public void unlikeTweet(Long tweetId, String loggedInUser) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);

        likesRepository.findByProfileIdAndTweetId(profileId, tweetId)
                .ifPresentOrElse(likesRepository::delete, () -> {
                    throw new EntityNotFoundException(
                            messageSourceService.generateMessage("error.entity.not_found", tweetId)
                    );
                });
    }

    public boolean isLiked(Long tweetId, String loggedInUser) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);

        return likesRepository.findByProfileIdAndTweetId(profileId, tweetId).isPresent();
    }
}
