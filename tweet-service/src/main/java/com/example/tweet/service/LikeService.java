package com.example.tweet.service;

import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.dto.response.TweetResponse;
import com.example.tweet.mapper.LikeMapper;
import com.example.tweet.mapper.TweetMapper;
import com.example.tweet.repository.LikeRepository;
import com.example.tweet.repository.TweetRepository;
import com.example.tweet.util.TweetUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.example.tweet.constants.EntityName.TWEETS;
import static com.example.tweet.constants.Operation.UPDATE;
import static com.example.tweet.constants.TopicName.HOME_TIMELINE_TOPIC;
import static com.example.tweet.constants.TopicName.USER_TIMELINE_TOPIC;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final TweetRepository tweetRepository;
    private final TweetUtil tweetUtil;
    private final TweetMapper tweetMapper;
    private final ProfileServiceClient profileServiceClient;
    private final LikeMapper likeMapper;
    private final MessageSourceService messageSourceService;

    public void likeTweet(Long tweetId, String loggedInUser) {
        tweetRepository.findById(tweetId)
                .map(tweet -> likeMapper.toEntity(tweet, profileServiceClient, loggedInUser))
                .map(likeRepository::saveAndFlush)
                .map(like -> {
                    TweetResponse tweetResponse = tweetMapper.toResponse(like.getParentTweet(), loggedInUser, tweetUtil, profileServiceClient);
                    tweetUtil.sendMessageToKafka(USER_TIMELINE_TOPIC, tweetResponse, TWEETS, UPDATE);
                    tweetUtil.sendMessageToKafka(HOME_TIMELINE_TOPIC, tweetResponse, TWEETS, UPDATE);
                    return like;
                })
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", tweetId)
                ));
    }

    public void unlikeTweet(Long tweetId, String loggedInUser) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);
        likeRepository.findByParentTweetIdAndProfileId(tweetId, profileId)
                .ifPresentOrElse(like -> {
                    likeRepository.delete(like);
                    TweetResponse tweetResponse = tweetMapper.toResponse(like.getParentTweet(), loggedInUser, tweetUtil, profileServiceClient);
                    tweetUtil.sendMessageToKafka(USER_TIMELINE_TOPIC, tweetResponse, TWEETS, UPDATE);
                    tweetUtil.sendMessageToKafka(HOME_TIMELINE_TOPIC, tweetResponse, TWEETS, UPDATE);
                }, () -> {
                    throw new EntityNotFoundException(
                            messageSourceService.generateMessage("error.entity.not_found", tweetId)
                    );
                });
    }
}
