package com.example.tweet.util;

import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.constants.EntityName;
import com.example.tweet.constants.Operation;
import com.example.tweet.dto.message.Message;
import com.example.tweet.dto.response.TweetResponse;
import com.example.tweet.entity.Tweet;
import com.example.tweet.exception.ActionNotAllowedException;
import com.example.tweet.producers.KafkaProducer;
import com.example.tweet.repository.LikeRepository;
import com.example.tweet.repository.TweetRepository;
import com.example.tweet.repository.ViewRepository;
import com.example.tweet.service.MessageSourceService;
import com.google.common.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TweetUtil {

    private final TweetRepository tweetRepository;
    private final LikeRepository likeRepository;
    private final ViewRepository viewRepository;
    private final KafkaProducer kafkaProducer;

    public int countRepliesForTweet(Long tweetId) {
        return tweetRepository.countAllByReplyToId(tweetId);
    }

    public int countLikesForTweet(Long tweetId) {
        return likeRepository.countAllByParentTweetId(tweetId);
    }

    public int countRetweetsForTweet(Long tweetId) {
        return tweetRepository.countAllByRetweetToId(tweetId);
    }

    public int countViewsForTweet(Long tweetId) {
        return viewRepository.countAllByParentTweetId(tweetId);
    }

    public boolean isTweetOwnedByLoggedInUser(
            Tweet tweet,
            String loggedInUser,
            ProfileServiceClient profileServiceClient,
            MessageSourceService messageSourceService
    ) {
        String profileIdOfLoggedInUser = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);
        if (!profileIdOfLoggedInUser.equals(tweet.getProfileId())) {
            throw new ActionNotAllowedException(
                    messageSourceService.generateMessage("error.action_not_allowed")
            );
        }
        return true;
    }

    public boolean isTweetRetweetedByLoggedInUser(Long retweetToId, String loggedInUser, ProfileServiceClient profileServiceClient) {
        String profileIdOfLoggedInUser = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);
        return tweetRepository.findByRetweetToIdAndProfileId(retweetToId, profileIdOfLoggedInUser).isPresent();
    }

    public boolean isTweetLikedByLoggedInUser(Long parentTweetId, String loggedInUser, ProfileServiceClient profileServiceClient) {
        String profileIdOfLoggedInUser = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);
        return likeRepository.findByParentTweetIdAndProfileId(parentTweetId, profileIdOfLoggedInUser).isPresent();
    }

    @SuppressWarnings("all")
    public TweetResponse sendMessageToKafka(String topic, TweetResponse entity, EntityName entityName, Operation operation) {
        Message<TweetResponse> message = Message.of(entity, entityName, operation);
        kafkaProducer.send(message, topic, new TypeToken<Message<TweetResponse>>(){}.getType());
        return entity;
    }
}
