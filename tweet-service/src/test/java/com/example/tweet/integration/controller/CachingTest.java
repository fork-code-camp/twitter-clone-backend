package com.example.tweet.integration.controller;


import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.client.StorageServiceClient;
import com.example.tweet.dto.request.TweetUpdateRequest;
import com.example.tweet.dto.response.TweetResponse;
import com.example.tweet.entity.Tweet;
import com.example.tweet.integration.IntegrationTestBase;
import com.example.tweet.integration.mocks.ProfileClientMock;
import com.example.tweet.repository.TweetRepository;
import com.example.tweet.service.ReplyService;
import com.example.tweet.service.RetweetService;
import com.example.tweet.service.TweetService;
import com.example.tweet.service.ViewService;
import com.example.tweet.util.TweetUtil;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.example.tweet.integration.constants.GlobalConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@RequiredArgsConstructor
@Sql(statements = "ALTER SEQUENCE tweets_id_seq RESTART WITH 1;")
@SuppressWarnings("all")
public class CachingTest extends IntegrationTestBase {

    private final CacheManager cacheManager;
    private final TweetService tweetService;
    private final RetweetService retweetService;
    private final ReplyService replyService;

    @MockBean
    private final TweetRepository tweetRepository;

    @MockBean
    private final TweetUtil tweetUtil;

    @MockBean
    private final ViewService viewService;

    @MockBean
    private final ProfileServiceClient profileServiceClient;

    @MockBean
    private final StorageServiceClient storageServiceClient;

    @BeforeEach
    public void setUp() {
        ProfileClientMock.setupProfileClientResponse(profileServiceClient);
        cacheManager.getCache("tweets").clear();
        cacheManager.getCache("retweets").clear();
        cacheManager.getCache("repliesForTweet").clear();
    }

    @Test
    public void cacheTweetTest() {
        createStubForTweet(1L, 25000, 5000, 1000, 1000);

        tweetService.getTweet(1L, EMAIL.getConstant());
        tweetService.getTweet(1L, EMAIL.getConstant());

        verify(tweetRepository, times(1))
                .findById(1L);

        TweetResponse tweetFromCache = getEntityFromCache(1L, TWEETS_CACHE_NAME.getConstant());
        assertNotNull(tweetFromCache);
        assertEquals(25000, tweetFromCache.getViews());
        assertEquals(5000, tweetFromCache.getLikes());
        assertEquals(1000, tweetFromCache.getRetweets());
        assertEquals(1000, tweetFromCache.getReplies());
    }

    @Test
    public void doNotCacheTweetTest() {
        createStubForTweet(1L, 1000, 250, 100, 100);

        tweetService.getTweet(1L, EMAIL.getConstant());
        tweetService.getTweet(1L, EMAIL.getConstant());

        verify(tweetRepository, times(2)).findById(1L);

        TweetResponse tweetFromCache = getEntityFromCache(1L, TWEETS_CACHE_NAME.getConstant());
        assertNull(tweetFromCache);
    }

    @Test
    public void updateTweetInCacheTest() {
        createStubForTweet(1L, 25000, 5000, 1000, 1000);

        tweetService.getTweet(1L, EMAIL.getConstant());
        TweetResponse tweetFromCache = getEntityFromCache(1L, TWEETS_CACHE_NAME.getConstant());
        assertEquals(DEFAULT_TWEET_TEXT.getConstant(), tweetFromCache.getText());

        tweetService.updateTweet(1L, new TweetUpdateRequest(UPDATE_TWEET_TEXT.getConstant()), EMAIL.getConstant(), null);
        TweetResponse updatedTweetFromCache = getEntityFromCache(1L, TWEETS_CACHE_NAME.getConstant());
        assertEquals(UPDATE_TWEET_TEXT.getConstant(), updatedTweetFromCache.getText());
    }

    @Test
    public void deleteTweetFromCacheTest() {
        createStubForTweet(1L, 25000, 5000, 1000, 1000);

        tweetService.getTweet(1L, EMAIL.getConstant());
        TweetResponse tweetFromCache = getEntityFromCache(1L, TWEETS_CACHE_NAME.getConstant());
        assertNotNull(tweetFromCache);

        tweetService.deleteTweet(1L, EMAIL.getConstant());
        TweetResponse deletedTweetFromCache = getEntityFromCache(1L, TWEETS_CACHE_NAME.getConstant());
        assertNull(deletedTweetFromCache);
    }

    @Test
    public void cacheRepliesForTweetTest() {
        Tweet tweetWithReplies = createStubForTweetWithReplies(1L, 0, 0, 0, 1000);

        replyService.findAllRepliesForTweet(1L);
        List<TweetResponse> repliesFromCache = getEntitiesFromCache(1L, REPLIES_CACHE_NAME.getConstant());
        assertNotNull(repliesFromCache);
        assertEquals(1000, repliesFromCache.size());
    }

    @Test
    public void cacheRetweetTest() {
        createStubForRetweet(1L, 1L, 25000, 5000, 1000, 1000);

        retweetService.findRetweetById(1L);
        TweetResponse retweet = retweetService.findRetweetById(1L);

        verify(tweetRepository, times(1)).findById(1L);

        TweetResponse retweetFromCache = getEntityFromCache(1L, RETWEETS_CACHE_NAME.getConstant());
        assertNotNull(retweetFromCache);
        assertEquals(retweet, retweetFromCache);
    }

    @Test
    public void deleteRetweetFromCache() {
        createStubForRetweet(1L, 1L, 25000, 5000, 1000, 1000);

        retweetService.findRetweetById(1L);
        TweetResponse retweetFromCache = getEntityFromCache(1L, RETWEETS_CACHE_NAME.getConstant());
        assertNotNull(retweetFromCache);

        retweetService.undoRetweet(1L, EMAIL.getConstant());
        TweetResponse deletedRetweetFromCache = getEntityFromCache(1L, RETWEETS_CACHE_NAME.getConstant());
        assertNull(deletedRetweetFromCache);
    }

    private Tweet createStubForTweetWithReplies(long tweetId, int views, int likes, int retweets, int replies) {
        Tweet parentTweet = createStubForTweet(tweetId, views, likes, retweets, replies);

        List<Tweet> repliesForTweetFromDb = mock(ArrayList.class);
        List<TweetResponse> repliesForTweet = mock(ArrayList.class);
        Stream<Tweet> mockStreamOfReplies = mock(Stream.class);

        when(tweetRepository.findAllByReplyToIdOrderByCreationDateDesc(tweetId))
                .thenReturn(repliesForTweetFromDb);

        when(repliesForTweetFromDb.stream())
                .thenReturn(mockStreamOfReplies);

        when(mockStreamOfReplies.map(any(Function.class)))
                .thenReturn(mockStreamOfReplies);

        when(mockStreamOfReplies.collect(any()))
                .thenReturn(repliesForTweet);

        when(repliesForTweet.size())
                .thenReturn(replies);

        return parentTweet;
    }

    private Tweet createStubForRetweet(long retweetToId, long retweetId, int views, int likes, int retweets, int replies) {
        Tweet parentTweet = createStubForTweet(retweetToId, views, likes, retweets, replies);
        Tweet retweet = buildDefaultRetweet(retweetId, parentTweet);

        when(tweetRepository.findById(retweetToId))
                .thenReturn(Optional.of(retweet));

        when(tweetRepository.findByProfileIdAndRetweetToId(ID.getConstant(), retweetToId))
                .thenReturn(Optional.of(retweet));

        doNothing()
                .when(tweetRepository)
                .delete(retweet);

        return retweet;
    }

    private Tweet createStubForTweet(long tweetId, int views, int likes, int retweets, int replies) {
        Tweet tweet = buildDefaultTweet(tweetId);

        when(tweetRepository.findById(tweetId))
                .thenReturn(Optional.of(tweet));

        when(tweetRepository.saveAndFlush(tweet))
                .thenReturn(tweet);

        when(viewService.createViewEntity(eq(tweet), anyString(), any()))
                .thenReturn(tweet);

        when(tweetUtil.countViewsForTweet(tweetId))
                .thenReturn(views);

        when(tweetUtil.countLikesForTweet(tweetId))
                .thenReturn(likes);

        when(tweetUtil.countRetweetsForTweet(tweetId))
                .thenReturn(retweets);

        when(tweetUtil.countRepliesForTweet(tweetId))
                .thenReturn(replies);

        return tweet;
    }

    @Nullable
    private TweetResponse getEntityFromCache(long entityId, String cacheName) {
        TweetResponse entityFromCache = cacheManager.getCache(cacheName).get(Long.toString(entityId), TweetResponse.class);
        return entityFromCache;
    }

    @Nullable
    private List<TweetResponse> getEntitiesFromCache(long parentEntityId, String cacheName) {
        List<TweetResponse> entitiesFromCache = cacheManager.getCache(cacheName).get(Long.toString(parentEntityId), List.class);
        return entitiesFromCache;
    }

    private Tweet buildDefaultRetweet(long retweetId, Tweet parentTweet) {
        Tweet retweet = Tweet.builder()
                .id(retweetId)
                .retweetTo(parentTweet)
                .creationDate(LocalDateTime.now())
                .profileId(ID.getConstant())
                .build();
        return retweet;
    }

    private Tweet buildDefaultTweet(long tweetId) {
        Tweet parentTweet = Tweet.builder()
                .id(tweetId)
                .text(DEFAULT_TWEET_TEXT.getConstant())
                .creationDate(LocalDateTime.now())
                .profileId(ID.getConstant())
                .build();
        return parentTweet;
    }
}
