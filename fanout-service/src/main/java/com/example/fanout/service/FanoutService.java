package com.example.fanout.service;

import com.example.fanout.client.ProfileServiceClient;
import com.example.fanout.constants.Operation;
import com.example.fanout.dto.message.EntityMessage;
import com.example.fanout.dto.response.ProfileResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FanoutService {

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

    public void processMessageForUserTimeline(EntityMessage entityMessage) {
        final Long entityId = entityMessage.entityId();
        final String timelineKey = TimelineCachePrefix.USER_TIMELINE_PREFIX.getPrefix().formatted(entityMessage.entityName()) + entityMessage.profileId();

        final Operation operation = Operation.valueOf(entityMessage.operation());
        switch (operation) {
            case ADD -> addEntityToTimeline(entityId, timelineKey);
            case DELETE -> deleteEntityFromTimeline(entityId, timelineKey);
        }
    }

    public void processMessageForHomeTimeline(EntityMessage entityMessage) {
        final Long entityId = entityMessage.entityId();
        List<ProfileResponse> followers = profileServiceClient.getFollowers(entityMessage.profileId());
        String prefix = TimelineCachePrefix.HOME_TIMELINE_PREFIX.getPrefix().formatted(entityMessage.entityName());

        final Operation operation = Operation.valueOf(entityMessage.operation());
        if (followers.size() < 10000) {
            for (ProfileResponse follower : followers) {
                final String timelineKey = prefix + follower.getProfileId();

                switch (operation) {
                    case ADD -> addEntityToTimeline(entityId, timelineKey);
                    case DELETE -> deleteEntityFromTimeline(entityId, timelineKey);
                }
            }
        }
    }

    private void addEntityToTimeline(Long entityId, String timelineKey) {
        List<Long> timeline = cacheService.getTimelineFromCache(timelineKey);
        if (timeline != null) {
            timeline.add(0, entityId);
            cacheService.cacheTimeline(timeline, timelineKey);
        }
    }

    private void deleteEntityFromTimeline(Long entityId, String timelineKey) {
        List<Long> timeline = cacheService.getTimelineFromCache(timelineKey);
        if (timeline != null) {
            timeline.removeIf(id -> id.equals(entityId));
            cacheService.cacheTimeline(timeline, timelineKey);
        }
    }
}
