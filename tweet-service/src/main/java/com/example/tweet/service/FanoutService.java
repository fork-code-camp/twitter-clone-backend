package com.example.tweet.service;

import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.dto.response.ProfileResponse;
import com.example.tweet.dto.response.TweetResponse;
import com.example.tweet.entity.Tweet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FanoutService {

    @Getter
    @AllArgsConstructor
    @ToString
    public enum EntityName {
        TWEETS("tweets"),
        RETWEETS("retweets"),
        REPLIES("replies");
        private final String name;
    }

    @AllArgsConstructor
    @Getter
    @ToString
    public enum TimelineCachePrefix {
        USER_TIMELINE_PREFIX("%s_user_timeline:"),
        HOME_TIMELINE_PREFIX("%s_home_timeline:");
        private final String prefix;
    }

    private final ProfileServiceClient profileServiceClient;
    private final CacheService cacheService;

    public TweetResponse addToUserTimeline(TweetResponse entity, EntityName entityName) {
        String prefix = TimelineCachePrefix.USER_TIMELINE_PREFIX.getPrefix().formatted(entityName.getName());
        addEntityToTimeline(entity, prefix + entity.getProfile().getProfileId());
        return entity;
    }

    public TweetResponse updateInUserTimeline(TweetResponse entity, EntityName entityName) {
        String prefix = TimelineCachePrefix.USER_TIMELINE_PREFIX.getPrefix().formatted(entityName.getName());
        updateEntityInTimeline(entity, prefix + entity.getProfile().getProfileId());
        return entity;
    }

    public void deleteFromUserTimeline(Tweet entity, EntityName entityName) {
        String prefix = TimelineCachePrefix.USER_TIMELINE_PREFIX.getPrefix().formatted(entityName.getName());
        deleteEntityFromTimeline(entity.getId(), prefix + entity.getProfileId());
    }

    public TweetResponse addToHomeTimelines(TweetResponse entity, EntityName entityName) {
        String prefix = TimelineCachePrefix.HOME_TIMELINE_PREFIX.getPrefix().formatted(entityName.getName());
        List<ProfileResponse> followers = profileServiceClient.getFollowers(entity.getProfile().getProfileId());
        if (followers.size() < 10000) {
            for (ProfileResponse follower : followers) {
                addEntityToTimeline(entity, prefix + follower.getProfileId());
            }
        }
        return entity;
    }

    public TweetResponse updateInHomeTimelines(TweetResponse entity, EntityName entityName) {
        String prefix = TimelineCachePrefix.HOME_TIMELINE_PREFIX.getPrefix().formatted(entityName.getName());
        List<ProfileResponse> followers = profileServiceClient.getFollowers(entity.getProfile().getProfileId());
        if (followers.size() < 10000) {
            for (ProfileResponse follower : followers) {
                updateEntityInTimeline(entity, prefix + follower.getProfileId());
            }
        }
        return entity;
    }

    public void deleteFromHomeTimelines(Tweet entity, EntityName entityName) {
        String prefix = TimelineCachePrefix.HOME_TIMELINE_PREFIX.getPrefix().formatted(entityName.getName());
        List<ProfileResponse> followers = profileServiceClient.getFollowers(entity.getProfileId());
        if (followers.size() < 10000) {
            for (ProfileResponse follower : followers) {
                deleteEntityFromTimeline(entity.getId(), prefix + follower.getProfileId());
            }
        }
    }

    private void addEntityToTimeline(TweetResponse entity, String timelineKey) {
        List<TweetResponse> timeline = cacheService.getTimelineFromCache(timelineKey);
        if (timeline != null) {
            timeline.add(0, entity);
            cacheService.cacheTimeline(timeline, timelineKey);
        }
    }

    private void deleteEntityFromTimeline(Long id, String timelineKey) {
        List<TweetResponse> timeline = cacheService.getTimelineFromCache(timelineKey);
        if (timeline != null) {
            timeline.removeIf(it -> it.getId().equals(id));
            cacheService.cacheTimeline(timeline, timelineKey);
        }
    }

    private void updateEntityInTimeline(TweetResponse entity, String timelineKey) {
        List<TweetResponse> timeline = cacheService.getTimelineFromCache(timelineKey);
        if (timeline != null) {
            timeline
                    .stream()
                    .filter(it -> it.getId().equals(entity.getId()))
                    .findFirst()
                    .ifPresent(tweetToUpdate -> tweetToUpdate.setText(entity.getText()));
            cacheService.cacheTimeline(timeline, timelineKey);
        }
    }
}
