package com.example.tweet.util;

import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.constant.EntityName;
import com.example.tweet.constant.Operation;
import com.example.tweet.dto.message.EntityMessage;
import com.example.tweet.dto.response.ProfileResponse;
import com.example.tweet.dto.response.TweetResponse;
import com.example.tweet.entity.Tweet;
import com.example.tweet.exception.ActionNotAllowedException;
import com.example.tweet.repository.LikeRepository;
import com.example.tweet.repository.TweetRepository;
import com.example.tweet.repository.ViewRepository;
import com.example.tweet.service.KafkaProducerService;
import com.example.tweet.service.MessageSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

import static com.example.tweet.constant.CacheName.*;
import static com.example.tweet.constant.EntityName.*;
import static com.example.tweet.constant.Operation.DELETE;
import static com.example.tweet.constant.TopicName.HOME_TIMELINE_TOPIC;
import static com.example.tweet.constant.TopicName.USER_TIMELINE_TOPIC;

@RequiredArgsConstructor
@Component
public class TweetUtil {

    public enum EvictionStrategy {CACHE_ONLY, WITH_TIMELINE}

    private final TweetRepository tweetRepository;
    private final LikeRepository likeRepository;
    private final ViewRepository viewRepository;
    private final KafkaProducerService kafkaProducerService;
    private final ProfileServiceClient profileServiceClient;
    private final MessageSourceService messageSourceService;
    private final CacheManager cacheManager;

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

    public boolean isEntityOwnedByLoggedInUser(Tweet entity, String loggedInUser) {
        String profileIdOfLoggedInUser = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);
        if (!profileIdOfLoggedInUser.equals(entity.getProfileId())) {
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

    public void sendMessageToKafka(String topic, Tweet entity, EntityName entityName, Operation operation) {
        EntityMessage entityMessage = EntityMessage.valueOf(entity.getId(), entity.getProfileId(), entityName, operation);
        kafkaProducerService.send(entityMessage, topic);
    }

    public void sendMessageWithTweet(Tweet tweet, Operation operation) {
        sendMessageToKafka(USER_TIMELINE_TOPIC, tweet, TWEETS, operation);
        sendMessageToKafka(HOME_TIMELINE_TOPIC, tweet, TWEETS, operation);
    }

    public void sendMessageWithRetweet(Tweet retweet, Operation operation) {
        sendMessageToKafka(USER_TIMELINE_TOPIC, retweet, RETWEETS, operation);
        sendMessageToKafka(HOME_TIMELINE_TOPIC, retweet, RETWEETS, operation);
    }

    public void sendMessageWithReply(Tweet reply, Operation operation) {
        sendMessageToKafka(USER_TIMELINE_TOPIC, reply, REPLIES, operation);
    }

    public TweetResponse updateProfileInResponse(TweetResponse response) {
        if (response.getRetweetTo() != null) {
            response.setRetweetTo(updateProfileInResponse(response.getRetweetTo()));
        }
        if (response.getReplyTo() != null) {
            response.setReplyTo(updateProfileInResponse(response.getReplyTo()));
        }
        if (response.getQuoteTo() != null) {
            response.setQuoteTo(updateProfileInResponse(response.getQuoteTo()));
        }

        ProfileResponse profile = profileServiceClient.getProfileById(response.getProfile().getProfileId());
        response.setProfile(profile);
        return response;
    }

    public void evictEntityFromCache(Long entityId, String cacheName) {
        Objects.requireNonNull(cacheManager.getCache(cacheName)).evictIfPresent(Long.toString(entityId));
    }

    public void evictAllEntityRelationsFromCache(Tweet entity, EvictionStrategy strategy) {
        Set<Tweet> retweets = entity.getRetweets();
        Set<Tweet> replies = entity.getReplies();
        List<Tweet> quotedTweets = tweetRepository.findAllByQuoteToId(entity.getId());

        evictEntitiesFromCache(retweets, RETWEETS_CACHE_NAME);
        evictEntitiesFromCache(replies, REPLIES_CACHE_NAME);
        evictEntitiesFromCache(quotedTweets, TWEETS_CACHE_NAME);

        if (Objects.requireNonNull(strategy) == EvictionStrategy.WITH_TIMELINE) {
            evictEntitiesFromTimelineCache(retweets, this::sendMessageWithRetweet);
            evictEntitiesFromTimelineCache(replies, this::sendMessageWithReply);
            evictEntitiesFromTimelineCache(quotedTweets, this::sendMessageWithTweet);
        }
    }

    private void evictEntitiesFromCache(Iterable<Tweet> entities, String cacheName) {
        for (Tweet entity : entities) {
            evictEntityFromCache(entity.getId(), cacheName);
        }
    }

    private void evictEntitiesFromTimelineCache(Iterable<Tweet> entities, BiConsumer<Tweet, Operation> evictionConsumer) {
        for (Tweet entity : entities) {
            evictionConsumer.accept(entity, DELETE);
        }
    }
}
