package com.example.timeline.service;

import com.example.timeline.client.ProfileServiceClient;
import com.example.timeline.client.TweetServiceClient;
import com.example.timeline.constants.EntityName;
import com.example.timeline.dto.response.ProfileResponse;
import com.example.timeline.dto.response.TweetResponse;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.function.Function3;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.example.timeline.constants.EntityName.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimelineService {

    @Getter
    @AllArgsConstructor
    @ToString
    private enum TimelineCachePrefix {
        USER_TIMELINE_PREFIX("%s_user_timeline:"),
        HOME_TIMELINE_PREFIX("%s_home_timeline:");

        private final String prefix;
    }

    private final ProfileServiceClient profileServiceClient;
    private final TweetServiceClient tweetServiceClient;
    private final CacheService cacheService;

    public List<TweetResponse> getUserTimelineForLoggedInUser(String loggedInUser, PageRequest page) {
        return getUserTimeline(profileServiceClient.getAuthProfile(loggedInUser), page);
    }

    public List<TweetResponse> getUserTimelineForAnotherInUser(String profileId, PageRequest page) {
        return getUserTimeline(profileServiceClient.getProfileById(profileId), page);
    }

    public List<TweetResponse> getRepliesUserTimelineForLoggedInUser(String loggedInUser, PageRequest page) {
        return getRepliesUserTimeline(profileServiceClient.getAuthProfile(loggedInUser), page);
    }

    public List<TweetResponse> getRepliesUserTimelineForAnotherInUser(String profileId, PageRequest page) {
        return getRepliesUserTimeline(profileServiceClient.getProfileById(profileId), page);
    }

    public List<TweetResponse> getHomeTimelineForLoggedInUser(String loggedInUser, PageRequest page) {
        ProfileResponse profile = profileServiceClient.getAuthProfile(loggedInUser);
        List<TweetResponse> tweets = getEntityHomeTimeline(profile, page, TWEETS, tweetServiceClient::getAllTweetsForUser, tweetServiceClient::getTweet);
        List<TweetResponse> retweets = getEntityHomeTimeline(profile, page, RETWEETS, tweetServiceClient::getAllRetweetsForUser, tweetServiceClient::getRetweet);
        return mergeTwoSortedLists(tweets, retweets);
    }

    private List<TweetResponse> getUserTimeline(ProfileResponse profile, PageRequest page) {
        List<TweetResponse> tweets = getEntityUserTimeline(profile, page, TWEETS, tweetServiceClient::getAllTweetsForUser, tweetServiceClient::getTweet);
        List<TweetResponse> retweets = getEntityUserTimeline(profile, page, RETWEETS, tweetServiceClient::getAllRetweetsForUser, tweetServiceClient::getRetweet);
        return mergeTwoSortedLists(tweets, retweets);
    }

    private List<TweetResponse> getRepliesUserTimeline(ProfileResponse profile, PageRequest page) {
        List<TweetResponse> replies = getEntityUserTimeline(profile, page, REPLIES, tweetServiceClient::getAllRepliesForUser, tweetServiceClient::getReply);
        List<TweetResponse> retweets = getEntityUserTimeline(profile, page, RETWEETS, tweetServiceClient::getAllRetweetsForUser, tweetServiceClient::getRetweet);
        return mergeTwoSortedLists(replies, retweets);
    }

    private List<TweetResponse> getEntityUserTimeline(
            ProfileResponse profile,
            PageRequest page,
            EntityName entityName,
            Function3<String, Integer, Integer, List<TweetResponse>> obtainEntitiesFromDbFunc,
            BiFunction<Long, String, TweetResponse> mapFunc
    ) {
        String timelineKey = TimelineCachePrefix.USER_TIMELINE_PREFIX.getPrefix().formatted(entityName.getName()) + profile.getProfileId();
        int seenNumberOfEntities = page.getPageNumber() * (page.getPageSize() / 2);

        List<TweetResponse> userTimeline = mapIdsToEntities(cacheService.getTimelineFromCache(timelineKey), profile.getEmail(), mapFunc);
        log.info("{} userTimeline received from cache", entityName.getName());

        if (userTimeline == null || (userTimeline.size() <= seenNumberOfEntities && userTimeline.size() > 0)) {
            log.info("{} userTimeline is null or its size is too small", entityName.getName());
            userTimeline = obtainEntitiesFromDbFunc.apply(profile.getProfileId(), 0, seenNumberOfEntities+100);

            cacheService.cacheTimeline(mapEntitiesToIds(userTimeline), timelineKey);
            log.info("{} userTimeline has been cached with size {}", entityName.getName(), userTimeline.size());
        }

        if (seenNumberOfEntities < userTimeline.size()) {
            userTimeline = userTimeline.subList(seenNumberOfEntities, Math.min(seenNumberOfEntities + (page.getPageSize() / 2), userTimeline.size()));
        }
        return userTimeline;
    }

    private List<TweetResponse> getEntityHomeTimeline(
            ProfileResponse profile,
            PageRequest page,
            EntityName entityName,
            Function3<String, Integer, Integer, List<TweetResponse>> obtainEntitiesFromDbFunc,
            BiFunction<Long, String, TweetResponse> mapFunc
    ) {
        String timelineKey = TimelineCachePrefix.HOME_TIMELINE_PREFIX.getPrefix().formatted(entityName.getName()) + profile.getProfileId();
        int seenNumberOfEntities = page.getPageNumber() * (page.getPageSize() / 2);

        List<TweetResponse> homeTimeline = mapIdsToEntities(cacheService.getTimelineFromCache(timelineKey), profile.getEmail(), mapFunc);
        log.info("{} homeTimeline received from cache", entityName.getName());

        if (homeTimeline == null || (homeTimeline.size() <= seenNumberOfEntities && homeTimeline.size() > 0)) {
            log.info("{} homeTimeline is null or its size is too small", entityName.getName());

            List<List<TweetResponse>> lists = new LinkedList<>();
            for (ProfileResponse followee : profileServiceClient.getFollowees(profile.getProfileId())) {
                if (followee.getFollowers() < 10000) {
                    lists.add(getEntityUserTimeline(
                            followee,
                            PageRequest.of(0, seenNumberOfEntities+page.getPageSize()),
                            entityName,
                            obtainEntitiesFromDbFunc,
                            mapFunc
                    ));
                }
            }
            if (!lists.isEmpty()) {
                homeTimeline = mergeKSortedLists(lists, 0, lists.size() - 1);
                cacheService.cacheTimeline(mapEntitiesToIds(homeTimeline), timelineKey);
                log.info("{} homeTimeline has been cached with size {}", entityName.getName(), homeTimeline.size());
            } else {
                homeTimeline = new LinkedList<>();
            }
        }

        if (seenNumberOfEntities < homeTimeline.size()) {
            homeTimeline = homeTimeline.subList(seenNumberOfEntities, Math.min(seenNumberOfEntities + (page.getPageSize() / 2), homeTimeline.size()));
        }

        for (ProfileResponse celebrity : profileServiceClient.getFolloweesCelebrities(profile.getProfileId())) {
            homeTimeline.addAll(getEntityUserTimeline(celebrity, page, entityName, obtainEntitiesFromDbFunc, mapFunc));
        }

        homeTimeline.sort((a,b) -> b.getCreationDate().compareTo(a.getCreationDate()));
        return homeTimeline;
    }

    private List<TweetResponse> mergeKSortedLists(List<List<TweetResponse>> lists, int l, int r) {
        if (l > r) {
            throw new IllegalArgumentException("left pointer should not be greater than right pointer");
        } else if (l == r) {
            return lists.get(l);
        }

        int mid = l + (r - l) / 2;
        List<TweetResponse> left = mergeKSortedLists(lists, l, mid);
        List<TweetResponse> right = mergeKSortedLists(lists, mid + 1, r);
        return mergeTwoSortedLists(left, right);
    }

    private List<TweetResponse> mergeTwoSortedLists(List<TweetResponse> list1, List<TweetResponse> list2) {
        List<TweetResponse> res = new LinkedList<>();
        int i = 0, j = 0;
        while (i < list1.size() && j < list2.size()) {
            if (list1.get(i).getCreationDate().isAfter(list2.get(j).getCreationDate())) {
                res.add(list1.get(i++));
            } else {
                res.add(list2.get(j++));
            }
        }

        while (i < list1.size()) {
            res.add(list1.get(i++));
        }
        while (j < list2.size()) {
            res.add(list2.get(j++));
        }

        return res;
    }

    private List<TweetResponse> mapIdsToEntities(List<Long> idList, String loggedInUser, BiFunction<Long, String, TweetResponse> mapFunc) {
        if (idList == null) {
            return null;
        }
        return idList.stream()
                .filter(Objects::nonNull)
                .map(id -> mapFunc.apply(id, loggedInUser))
                .collect(Collectors.toList());
    }

    private List<Long> mapEntitiesToIds(@NonNull List<TweetResponse> entities) {
        return entities.stream()
                .map(TweetResponse::getId)
                .collect(Collectors.toList());
    }
}
