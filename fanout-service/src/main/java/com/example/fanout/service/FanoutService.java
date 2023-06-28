package com.example.fanout.service;

import com.example.fanout.client.ProfileServiceClient;
import com.example.fanout.constants.Operation;
import com.example.fanout.dto.message.Message;
import com.example.fanout.dto.response.ProfileResponse;
import com.example.fanout.dto.response.TweetResponse;
import com.example.fanout.mapper.EntityMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FanoutService {

    private final EntityMapper entityMapper;

    @AllArgsConstructor
    @Getter
    @ToString
    private enum TimelineCachePrefix {
        USER_TIMELINE_PREFIX("%s_user_timeline:"),
        HOME_TIMELINE_PREFIX("%s_home_timeline:");
        private final String prefix;
    }

    private final ProfileServiceClient profileServiceClient;
    private final CacheService cacheService;

    public void processMessageForUserTimeline(Message<TweetResponse> message) {
        final TweetResponse entity = message.getEntity();
        final String timelineKey = TimelineCachePrefix.USER_TIMELINE_PREFIX.getPrefix().formatted(message.getEntityName()) + entity.getProfile().getProfileId();

        final Operation operation = Operation.valueOf(message.getOperation());
        switch (operation) {
            case ADD -> addEntityToTimeline(entity, timelineKey);
            case UPDATE -> updateEntityInTimeline(entity, timelineKey);
            case DELETE -> deleteEntityFromTimeline(entity.getId(), timelineKey);
        }
    }

    public void processMessageForHomeTimeline(Message<TweetResponse> message) {
        final TweetResponse entity = message.getEntity();
        List<ProfileResponse> followers = profileServiceClient.getFollowers(entity.getProfile().getProfileId());
        String prefix = TimelineCachePrefix.HOME_TIMELINE_PREFIX.getPrefix().formatted(message.getEntityName());

        final Operation operation = Operation.valueOf(message.getOperation());
        if (followers.size() < 10000) {
            for (ProfileResponse follower : followers) {
                final String timelineKey = prefix + follower.getProfileId();

                switch (operation) {
                    case ADD -> addEntityToTimeline(entity, timelineKey);
                    case UPDATE -> updateEntityInTimeline(entity, timelineKey);
                    case DELETE -> deleteEntityFromTimeline(entity.getId(), timelineKey);
                }
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
                    .ifPresent(entityToUpdate -> entityMapper.updateEntity(entity, entityToUpdate));
            cacheService.cacheTimeline(timeline, timelineKey);
        }
    }
}
