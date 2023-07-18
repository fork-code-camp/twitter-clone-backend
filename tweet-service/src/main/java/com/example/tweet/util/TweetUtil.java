package com.example.tweet.util;

import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.constant.EntityName;
import com.example.tweet.constant.Operation;
import com.example.tweet.dto.message.EntityMessage;
import com.example.tweet.dto.response.TweetResponse;
import com.example.tweet.entity.Tweet;
import com.example.tweet.exception.ActionNotAllowedException;
import com.example.tweet.repository.LikeRepository;
import com.example.tweet.repository.TweetRepository;
import com.example.tweet.repository.ViewRepository;
import com.example.tweet.service.KafkaProducerService;
import com.example.tweet.service.MessageSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.example.tweet.constant.EntityName.*;
import static com.example.tweet.constant.TopicName.HOME_TIMELINE_TOPIC;
import static com.example.tweet.constant.TopicName.USER_TIMELINE_TOPIC;

@RequiredArgsConstructor
@Component
public class TweetUtil {

    private final TweetRepository tweetRepository;
    private final LikeRepository likeRepository;
    private final ViewRepository viewRepository;
    private final KafkaProducerService kafkaProducerService;

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

    public void sendMessageToKafka(String topic, TweetResponse entity, EntityName entityName, Operation operation) {
        EntityMessage entityMessage = EntityMessage.valueOf(entity.getId(), entity.getProfile().getProfileId(), entityName, operation);
        kafkaProducerService.send(entityMessage, topic);
    }

    public void sendMessageWithTweet(TweetResponse tweet,  Operation operation) {
        sendMessageToKafka(USER_TIMELINE_TOPIC, tweet, TWEETS, operation);
        sendMessageToKafka(HOME_TIMELINE_TOPIC, tweet, TWEETS, operation);
    }

    public void sendMessageWithRetweet(TweetResponse retweet, Operation operation) {
        sendMessageToKafka(USER_TIMELINE_TOPIC, retweet, RETWEETS, operation);
        sendMessageToKafka(HOME_TIMELINE_TOPIC, retweet, RETWEETS, operation);
    }

    public void sendMessageWithReply(TweetResponse reply, Operation operation) {
        sendMessageToKafka(USER_TIMELINE_TOPIC, reply, REPLIES, operation);
    }
}
