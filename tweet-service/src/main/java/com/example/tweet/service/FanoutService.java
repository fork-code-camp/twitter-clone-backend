package com.example.tweet.service;

import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.client.response.ProfileResponse;
import com.example.tweet.dto.response.RetweetResponse;
import com.example.tweet.dto.response.TweetResponse;
import com.example.tweet.entity.BaseEntity;
import com.example.tweet.entity.Retweet;
import com.example.tweet.entity.Tweet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.tweet.service.FanoutService.TimelinePrefixes.*;

@Service
@RequiredArgsConstructor
public class FanoutService {

    @AllArgsConstructor
    @Getter
    @ToString
    public enum TimelinePrefixes {
        TWEETS_USER_TIMELINE_PREFIX("tweets_user_timeline:"),
        TWEETS_HOME_TIMELINE_PREFIX("tweets_home_timeline:"),
        RETWEETS_USER_TIMELINE_PREFIX("retweets_user_timeline:"),
        RETWEETS_HOME_TIMELINE_PREFIX("retweets_home_timeline:"),
        REPLIES_USER_TIMELINE_PREFIX("replies_user_timeline:");

        private final String prefix;
    }

    private final ProfileServiceClient profileServiceClient;
    private final RedisTemplate<String, Object> redisTemplate;

    @Service
    public class UserTimelineService {
        public TweetResponse addTweetToUserTimeline(TweetResponse tweetResponse) {
            if (tweetResponse.getReplyTo() == null) {
                addEntityToTimeline(tweetResponse, TWEETS_USER_TIMELINE_PREFIX.prefix + tweetResponse.getProfile().profileId());
            } else {
                addEntityToTimeline(tweetResponse, REPLIES_USER_TIMELINE_PREFIX.prefix + tweetResponse.getProfile().profileId());
            }
            return tweetResponse;
        }

        public TweetResponse updateTweetInUserTimeline(TweetResponse tweetResponse) {
            if (tweetResponse.getReplyTo() == null) {
                updateTweetInTimeline(tweetResponse, TWEETS_USER_TIMELINE_PREFIX.prefix + tweetResponse.getProfile().profileId());
            } else {
                updateTweetInTimeline(tweetResponse, REPLIES_USER_TIMELINE_PREFIX.prefix + tweetResponse.getProfile().profileId());
            }
            return tweetResponse;
        }

        public void deleteTweetFromUserTimeline(Tweet tweet) {
            if (tweet.getReplyTo() == null) {
                deleteEntityFromTimeline(tweet.getId(), TWEETS_USER_TIMELINE_PREFIX.prefix + tweet.getProfileId());
            } else {
                deleteEntityFromTimeline(tweet.getId(), REPLIES_USER_TIMELINE_PREFIX.prefix + tweet.getProfileId());
            }
        }

        public RetweetResponse addRetweetToUserTimeline(RetweetResponse retweetResponse) {
            addEntityToTimeline(retweetResponse, RETWEETS_USER_TIMELINE_PREFIX.prefix + retweetResponse.getProfile().profileId());
            return retweetResponse;
        }

        public void deleteRetweetFromUserTimeline(Retweet retweet) {
            deleteEntityFromTimeline(retweet.getId(), RETWEETS_USER_TIMELINE_PREFIX.prefix + retweet.getProfileId());
        }
    }

    @Service
    class HomeTimelineService {
        public TweetResponse addTweetToHomeTimelines(TweetResponse tweetResponse) {
            List<ProfileResponse> followers = profileServiceClient.getFollowers(tweetResponse.getProfile().profileId());
            if (followers.size() <= 10000) {
                for (ProfileResponse follower : followers) {
                    addEntityToTimeline(tweetResponse, TWEETS_HOME_TIMELINE_PREFIX.prefix + follower.profileId());
                }
            }
            return tweetResponse;
        }

        public TweetResponse updateTweetInHomeTimelines(TweetResponse tweetResponse) {
            List<ProfileResponse> followers = profileServiceClient.getFollowers(tweetResponse.getProfile().profileId());
            if (followers.size() <= 10000) {
                for (ProfileResponse follower : followers) {
                    updateTweetInTimeline(tweetResponse, TWEETS_HOME_TIMELINE_PREFIX.prefix + follower.profileId());
                }
            }
            return tweetResponse;
        }

        public void deleteTweetFromHomeTimelines(Tweet tweet) {
            List<ProfileResponse> followers = profileServiceClient.getFollowers(tweet.getProfileId());
            if (followers.size() <= 10000) {
                for (ProfileResponse follower : followers) {
                    deleteEntityFromTimeline(tweet.getId(), TWEETS_HOME_TIMELINE_PREFIX.prefix + follower.profileId());
                }
            }
        }

        public RetweetResponse addRetweetToHomeTimelines(RetweetResponse retweetResponse) {
            List<ProfileResponse> followers = profileServiceClient.getFollowers(retweetResponse.getProfile().profileId());
            if (followers.size() <= 10000) {
                for (ProfileResponse follower : followers) {
                    addEntityToTimeline(retweetResponse, RETWEETS_HOME_TIMELINE_PREFIX.prefix + follower.profileId());
                }
            }
            return retweetResponse;
        }

        public void deleteRetweetFromHomeTimelines(Retweet retweet) {
            List<ProfileResponse> followers = profileServiceClient.getFollowers(retweet.getProfileId());
            if (followers.size() <= 10000) {
                for (ProfileResponse follower : followers) {
                    deleteEntityFromTimeline(retweet.getId(), RETWEETS_HOME_TIMELINE_PREFIX.prefix + follower.profileId());
                }
            }
        }
    }

    private <T> void addEntityToTimeline(T entity, String timelineKey) {
        List<T> timeline = getTimelineFromCache(timelineKey);
        if (timeline != null) {
            timeline.add(0, entity);
            setTimelineToCache(timeline, timelineKey);
        }
    }

    private <T extends BaseEntity<Long>> void deleteEntityFromTimeline(Long entityId, String timelineKey) {
        List<T> timeline = getTimelineFromCache(timelineKey);
        if (timeline != null) {
            timeline.removeIf(it -> it.getId().equals(entityId));
            setTimelineToCache(timeline, timelineKey);
        }
    }

    private void updateTweetInTimeline(TweetResponse tweetResponse, String timelineKey) {
        List<TweetResponse> timeline = getTimelineFromCache(timelineKey);
        if (timeline != null) {
            timeline
                    .stream()
                    .filter(it -> it.getTweetId().equals(tweetResponse.getTweetId()))
                    .findFirst()
                    .ifPresent(tweetToUpdate -> tweetToUpdate.setText(tweetResponse.getText()));
            setTimelineToCache(timeline, timelineKey);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private <T> List<T> getTimelineFromCache(String timelineKey) {
        return (List<T>) redisTemplate.opsForValue().get(timelineKey);
    }

    private <T> void setTimelineToCache(List<T> timeline, String timelineKey) {
        redisTemplate.opsForValue().set(timelineKey, timeline, 14, TimeUnit.DAYS);
    }
}
