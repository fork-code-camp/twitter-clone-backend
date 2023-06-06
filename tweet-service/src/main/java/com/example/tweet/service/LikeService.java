package com.example.tweet.service;

import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.entity.Tweet;
import com.example.tweet.exception.CreateEntityException;
import com.example.tweet.mapper.LikeMapper;
import com.example.tweet.repository.LikeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final TweetService tweetService;
    private final ProfileServiceClient profileServiceClient;
    private final LikeMapper likeMapper;
    private final MessageSourceService messageSourceService;

    public void likeTweet(Long parentTweetId, String loggedInUser) {
        Tweet parentTweet = tweetService.getTweetEntityById(parentTweetId);
        Optional.of(parentTweet)
                .map(tweet -> likeMapper.toEntity(tweet, profileServiceClient, loggedInUser))
                .map(likeRepository::saveAndFlush)
                .orElseThrow(() -> new CreateEntityException(
                        messageSourceService.generateMessage("error.entity.unsuccessful_creation")
                ));
    }

    public void unlikeTweet(Long tweetId, String loggedInUser) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);
        likeRepository.findByProfileIdAndParentTweetId(profileId, tweetId)
                .ifPresentOrElse(likeRepository::delete, () -> {
                    throw new EntityNotFoundException(
                            messageSourceService.generateMessage("error.entity.not_found", tweetId)
                    );
                });
    }

    public boolean isLiked(Long tweetId, String loggedInUser) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);
        return likeRepository.findByProfileIdAndParentTweetId(profileId, tweetId).isPresent();
    }
}
