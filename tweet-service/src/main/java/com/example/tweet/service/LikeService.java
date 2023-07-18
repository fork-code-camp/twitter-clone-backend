package com.example.tweet.service;

import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.mapper.LikeMapper;
import com.example.tweet.repository.LikeRepository;
import com.example.tweet.repository.TweetRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final TweetRepository tweetRepository;
    private final ProfileServiceClient profileServiceClient;
    private final LikeMapper likeMapper;
    private final MessageSourceService messageSourceService;

    public void likeTweet(Long tweetId, String loggedInUser) {
        tweetRepository.findById(tweetId)
                .map(tweet -> likeMapper.toEntity(tweet, profileServiceClient, loggedInUser))
                .map(likeRepository::saveAndFlush)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", tweetId)
                ));
    }

    public void unlikeTweet(Long tweetId, String loggedInUser) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);
        likeRepository.findByParentTweetIdAndProfileId(tweetId, profileId)
                .ifPresentOrElse(likeRepository::delete, () -> {
                    throw new EntityNotFoundException(
                            messageSourceService.generateMessage("error.entity.not_found", tweetId)
                    );
                });
    }
}
