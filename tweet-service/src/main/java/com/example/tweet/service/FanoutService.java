package com.example.tweet.service;

import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.client.response.ProfileResponse;
import com.example.tweet.dto.response.TweetResponse;
import com.example.tweet.entity.Tweet;
import lombok.*;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class FanoutService {

    @Getter
    @AllArgsConstructor
    @ToString
    public enum EntityCachePrefix {
        TWEETS("tweets"),
        RETWEETS("retweets"),
        REPLIES("replies");
        private final String prefix;
    }

    @AllArgsConstructor
    @Getter
    @ToString
    private enum TimelinePrefix {
        USER_TIMELINE_PREFIX("%s_user_timeline:"),
        HOME_TIMELINE_PREFIX("%s_home_timeline:");
        private final String prefix;
    }

    private final ProfileServiceClient profileServiceClient;
    private final RedisTemplate<String, Object> redisTemplate;

    public TweetResponse addToUserTimeline(TweetResponse entity, EntityCachePrefix entityCachePrefix) {
        String prefix = TimelinePrefix.USER_TIMELINE_PREFIX.getPrefix().formatted(entityCachePrefix.getPrefix());
        addEntityToTimeline(entity, prefix + entity.getProfile().profileId());
        return entity;
    }

    public TweetResponse updateInUserTimeline(TweetResponse entity, EntityCachePrefix entityCachePrefix) {
        String prefix = TimelinePrefix.USER_TIMELINE_PREFIX.getPrefix().formatted(entityCachePrefix.getPrefix());
        updateEntityInTimeline(entity, prefix + entity.getProfile().profileId());
        return entity;
    }

    public void deleteFromUserTimeline(Tweet entity, EntityCachePrefix entityCachePrefix) {
        String prefix = TimelinePrefix.USER_TIMELINE_PREFIX.getPrefix().formatted(entityCachePrefix.getPrefix());
        deleteEntityFromTimeline(entity.getId(), prefix + entity.getProfileId());
    }

    public TweetResponse addToHomeTimelines(TweetResponse entity, EntityCachePrefix entityCachePrefix) {
        String prefix = TimelinePrefix.HOME_TIMELINE_PREFIX.getPrefix().formatted(entityCachePrefix.getPrefix());
        List<ProfileResponse> followers = profileServiceClient.getFollowers(entity.getProfile().profileId());
        if (followers.size() < 10000) {
            for (ProfileResponse follower : followers) {
                addEntityToTimeline(entity, prefix + follower.profileId());
            }
        }
        return entity;
    }

    public TweetResponse updateInHomeTimelines(TweetResponse entity, EntityCachePrefix entityCachePrefix) {
        String prefix = TimelinePrefix.HOME_TIMELINE_PREFIX.getPrefix().formatted(entityCachePrefix.getPrefix());
        List<ProfileResponse> followers = profileServiceClient.getFollowers(entity.getProfile().profileId());
        if (followers.size() < 10000) {
            for (ProfileResponse follower : followers) {
                updateEntityInTimeline(entity, prefix + follower.profileId());
            }
        }
        return entity;
    }

    public void deleteFromHomeTimelines(Tweet entity, EntityCachePrefix entityCachePrefix) {
        String prefix = TimelinePrefix.HOME_TIMELINE_PREFIX.getPrefix().formatted(entityCachePrefix.getPrefix());
        List<ProfileResponse> followers = profileServiceClient.getFollowers(entity.getProfileId());
        if (followers.size() < 10000) {
            for (ProfileResponse follower : followers) {
                deleteEntityFromTimeline(entity.getId(), prefix + follower.profileId());
            }
        }
    }

    private void addEntityToTimeline(TweetResponse entity, String timelineKey) {
        List<TweetResponse> timeline = getTimelineFromCache(timelineKey);
        if (timeline != null) {
            timeline.add(0, entity);
            setTimelineToCache(timeline, timelineKey);
        }
    }

    private void deleteEntityFromTimeline(Long id, String timelineKey) {
        List<TweetResponse> timeline = getTimelineFromCache(timelineKey);
        if (timeline != null) {
            timeline.removeIf(it -> it.getId().equals(id));
            setTimelineToCache(timeline, timelineKey);
        }
    }

    private void updateEntityInTimeline(TweetResponse entity, String timelineKey) {
        List<TweetResponse> timeline = getTimelineFromCache(timelineKey);
        if (timeline != null) {
            timeline
                    .stream()
                    .filter(it -> it.getId().equals(entity.getId()))
                    .findFirst()
                    .ifPresent(tweetToUpdate -> tweetToUpdate.setText(entity.getText()));
            setTimelineToCache(timeline, timelineKey);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private List<TweetResponse> getTimelineFromCache(String timelineKey) {
        return (List<TweetResponse>) redisTemplate.opsForValue().get(timelineKey);
    }

    private void setTimelineToCache(List<TweetResponse> timeline, String timelineKey) {
        redisTemplate.opsForValue().set(timelineKey, timeline, 14, TimeUnit.DAYS);
    }
}
