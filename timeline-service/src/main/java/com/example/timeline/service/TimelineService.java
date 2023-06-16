package com.example.timeline.service;

import com.example.timeline.client.ProfileServiceClient;
import com.example.timeline.client.TweetServiceClient;
import com.example.timeline.dto.response.ProfileResponse;
import com.example.timeline.dto.response.TweetResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.function.Function3;

import java.util.LinkedList;
import java.util.List;

import static com.example.timeline.service.TimelineService.EntityName.*;
import static com.example.timeline.service.TimelineService.TimelineCachePrefix.HOME_TIMELINE_PREFIX;
import static com.example.timeline.service.TimelineService.TimelineCachePrefix.USER_TIMELINE_PREFIX;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimelineService {

    @Getter
    @AllArgsConstructor
    @ToString
    public enum TimelineCachePrefix {
        USER_TIMELINE_PREFIX("%s_user_timeline:"),
        HOME_TIMELINE_PREFIX("%s_home_timeline:");

        private final String prefix;
    }

    @Getter
    @AllArgsConstructor
    @ToString
    public enum EntityName {
        TWEETS("tweets"),
        RETWEETS("retweets"),
        REPLIES("replies");
        private final String name;
    }

    private final ProfileServiceClient profileServiceClient;
    private final TweetServiceClient tweetServiceClient;
    private final CacheService cacheService;

    public List<TweetResponse> getUserTimeline(String loggedInUser, PageRequest page) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);
        List<TweetResponse> tweets = getEntityUserTimeline(profileId, loggedInUser, page, TWEETS, tweetServiceClient::getAllTweetsForUser);
        List<TweetResponse> retweets = getEntityUserTimeline(profileId, loggedInUser, page, RETWEETS, tweetServiceClient::getAllRetweetsForUser);
        return mergeTwoSortedLists(tweets, retweets);
    }

    public List<TweetResponse> getRepliesUserTimeline(String loggedInUser, PageRequest page) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);
        List<TweetResponse> replies = getEntityUserTimeline(profileId, loggedInUser, page, REPLIES, tweetServiceClient::getAllRepliesForUser);
        List<TweetResponse> retweets = getEntityUserTimeline(profileId, loggedInUser, page, RETWEETS, tweetServiceClient::getAllRetweetsForUser);
        return mergeTwoSortedLists(replies, retweets);
    }

    public List<TweetResponse> getHomeTimeline(String loggedInUser, PageRequest page) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);
        List<TweetResponse> tweets = getEntityHomeTimeline(profileId, page, TWEETS, tweetServiceClient::getAllTweetsForUser);
        List<TweetResponse> retweets = getEntityHomeTimeline(profileId, page, RETWEETS, tweetServiceClient::getAllRetweetsForUser);
        return mergeTwoSortedLists(tweets, retweets);
    }

    private List<TweetResponse> getEntityUserTimeline(
            String profileId,
            String loggedInUser,
            PageRequest page,
            EntityName entityName,
            Function3<String, Integer, Integer, List<TweetResponse>> function
    ) {
        String timelineKey = USER_TIMELINE_PREFIX.getPrefix().formatted(entityName.getName()) + profileId;
        int seenNumberOfEntities = page.getPageNumber() * (page.getPageSize() / 2);

        List<TweetResponse> userTimeline = cacheService.getTimelineFromCache(timelineKey);
        log.info("{} userTimeline received from cache", entityName.getName());

        if (userTimeline == null || userTimeline.size() <= seenNumberOfEntities) {
            log.info("{} userTimeline is null or its size is too small", entityName.getName());

            userTimeline = function.apply(loggedInUser, 0, seenNumberOfEntities+100);
            cacheService.cacheTimeline(userTimeline, timelineKey);

            log.info("{} userTimeline has been cached with size {}", entityName.getName(), userTimeline.size());
        }

        if (seenNumberOfEntities < userTimeline.size()) {
            userTimeline = userTimeline.subList(seenNumberOfEntities, Math.min(seenNumberOfEntities + (page.getPageSize() / 2), userTimeline.size()));
        }
        return userTimeline;
    }

    private List<TweetResponse> getEntityHomeTimeline(
            String profileId,
            PageRequest page,
            EntityName entityName,
            Function3<String, Integer, Integer, List<TweetResponse>> function
    ) {
        String timelineKey = HOME_TIMELINE_PREFIX.getPrefix().formatted(entityName.getName()) + profileId;
        int seenNumberOfEntities = page.getPageNumber() * (page.getPageSize() / 2);

        List<TweetResponse> homeTimeline = cacheService.getTimelineFromCache(timelineKey);
        log.info("{} homeTimeline received from cache", entityName.getName());

        if (homeTimeline == null || homeTimeline.size() <= seenNumberOfEntities) {
            log.info("{} homeTimeline is null or its size is too small", entityName.getName());

            List<List<TweetResponse>> lists = new LinkedList<>();
            for (ProfileResponse followee : profileServiceClient.getFollowees(profileId)) {
                if (followee.getFollowers() < 10000) {
                    lists.add(getEntityUserTimeline(
                            followee.getProfileId(), followee.getEmail(), PageRequest.of(0, seenNumberOfEntities+page.getPageSize()), entityName, function
                    ));
                }
            }
            if (!lists.isEmpty()) {
                homeTimeline = mergeKSortedLists(lists, 0, lists.size() - 1);

                cacheService.cacheTimeline(homeTimeline, timelineKey);
                log.info("{} homeTimeline has been cached with size {}", entityName.getName(), homeTimeline.size());
            } else {
                homeTimeline = new LinkedList<>();
            }
        }

        if (seenNumberOfEntities < homeTimeline.size()) {
            homeTimeline = homeTimeline.subList(seenNumberOfEntities, Math.min(seenNumberOfEntities + (page.getPageSize() / 2), homeTimeline.size()));
        }

        for (ProfileResponse celebrity : profileServiceClient.getFolloweesCelebrities(profileId)) {
            homeTimeline.addAll(getEntityUserTimeline(celebrity.getProfileId(), celebrity.getEmail(), page, entityName, function));
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
}
