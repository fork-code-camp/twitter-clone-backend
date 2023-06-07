package com.example.tweet.integration.controller;


import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.dto.request.TweetUpdateRequest;
import com.example.tweet.dto.response.RetweetResponse;
import com.example.tweet.dto.response.TweetResponse;
import com.example.tweet.entity.Retweet;
import com.example.tweet.entity.Tweet;
import com.example.tweet.integration.IntegrationTestBase;
import com.example.tweet.integration.mocks.ProfileClientMock;
import com.example.tweet.repository.RetweetRepository;
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
    private final RetweetRepository retweetRepository;

    @MockBean
    private final TweetUtil tweetUtil;

    @MockBean
    private final ViewService viewService;

    @MockBean
    private final ProfileServiceClient profileServiceClient;

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

        TweetResponse tweetFromCache = getTweetFromCache(1L);
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

        TweetResponse tweetFromCache = getTweetFromCache(1L);
        assertNull(tweetFromCache);
    }

    @Test
    public void updateTweetInCacheTest() {
        createStubForTweet(1L, 25000, 5000, 1000, 1000);

        tweetService.getTweet(1L, EMAIL.getConstant());
        TweetResponse tweetFromCache = getTweetFromCache(1L);
        assertEquals(DEFAULT_TWEET_TEXT.getConstant(), tweetFromCache.getText());

        tweetService.updateTweet(1L, new TweetUpdateRequest(UPDATE_TWEET_TEXT.getConstant()), EMAIL.getConstant());
        TweetResponse updatedTweetFromCache = getTweetFromCache(1L);
        assertEquals(UPDATE_TWEET_TEXT.getConstant(), updatedTweetFromCache.getText());
    }

    @Test
    public void deleteTweetFromCacheTest() {
        createStubForTweet(1L, 25000, 5000, 1000, 1000);

        tweetService.getTweet(1L, EMAIL.getConstant());
        TweetResponse tweetFromCache = getTweetFromCache(1L);
        assertNotNull(tweetFromCache);

        tweetService.deleteTweet(1L, EMAIL.getConstant());
        TweetResponse deletedTweetFromCache = getTweetFromCache(1L);
        assertNull(deletedTweetFromCache);
    }

    @Test
    public void cacheRepliesForTweetTest() {
        Tweet tweetWithReplies = createStubForTweetWithReplies(1L, 0, 0, 0, 1000);

        replyService.findAllRepliesForTweet(1L);
        List<TweetResponse> repliesFromCache = getRepliesFromCache(1L);
        assertNotNull(repliesFromCache);
        assertEquals(1000, repliesFromCache.size());
    }

    @Test
    public void cacheRetweetTest() {
        createStubForRetweet(1L, 1L, 25000, 5000, 1000, 1000);

        retweetService.findRetweetById(1L);
        RetweetResponse retweet = retweetService.findRetweetById(1L);

        verify(retweetRepository, times(1)).findById(1L);

        RetweetResponse retweetFromCache = getRetweetFromCache(1L);
        assertNotNull(retweetFromCache);
        assertEquals(retweet, retweetFromCache);
    }

    @Test
    public void deleteRetweetFromCache() {
        createStubForRetweet(1L, 1L, 25000, 5000, 1000, 1000);

        retweetService.findRetweetById(1L);
        RetweetResponse retweetFromCache = getRetweetFromCache(1L);
        assertNotNull(retweetFromCache);

        retweetService.undoRetweet(1L, EMAIL.getConstant());
        RetweetResponse deletedRetweetFromCache = getRetweetFromCache(1L);
        assertNull(deletedRetweetFromCache);
    }

    private Tweet createStubForTweetWithReplies(long tweetId, int views, int likes, int retweets, int replies) {
        Tweet parentTweet = createStubForTweet(tweetId, views, likes, retweets, replies);

        List<Tweet> repliesForTweetFromDb = mock(ArrayList.class);
        List<TweetResponse> repliesForTweet = mock(ArrayList.class);
        Stream<Tweet> mockStreamOfReplies = mock(Stream.class);

        when(tweetRepository.findAllByReplyToId(tweetId))
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

    private Retweet createStubForRetweet(long parentTweetId, long retweetId, int views, int likes, int retweets, int replies) {
        Tweet parentTweet = createStubForTweet(parentTweetId, views, likes, retweets, replies);
        Retweet retweet = buildDefaultRetweet(retweetId, parentTweet);

        doReturn(Optional.of(retweet))
                .when(retweetRepository)
                .findById(retweetId);

        doReturn(Optional.of(retweet))
                .when(retweetRepository)
                .findByParentTweetIdAndProfileId(parentTweetId, ID.getConstant());

        doNothing()
                .when(retweetRepository)
                .delete(retweet);

        return retweet;
    }

    private Tweet createStubForTweet(long tweetId, int views, int likes, int retweets, int replies) {
        Tweet parentTweet = buildDefaultTweet(tweetId);

        doReturn(Optional.of(parentTweet))
                .when(tweetRepository)
                .findById(tweetId);

        doReturn(parentTweet)
                .when(tweetRepository)
                .saveAndFlush(any());

        doReturn(parentTweet)
                .when(viewService)
                .createViewEntity(any(Tweet.class), anyString(), any());

        doReturn(views)
                .when(tweetUtil)
                .countViewsForTweet(tweetId);

        doReturn(likes)
                .when(tweetUtil)
                .countLikesForTweet(tweetId);

        doReturn(retweets)
                .when(tweetUtil)
                .countRetweetsForTweet(tweetId);

        doReturn(replies)
                .when(tweetUtil)
                .countRepliesForTweet(tweetId);

        return parentTweet;
    }

    @Nullable
    private TweetResponse getTweetFromCache(long tweetId) {
        TweetResponse tweetFromCache = cacheManager.getCache("tweets").get(Long.toString(tweetId), TweetResponse.class);
        return tweetFromCache;
    }

    @Nullable
    private RetweetResponse getRetweetFromCache(long retweetId) {
        RetweetResponse retweetFromCache = cacheManager.getCache("retweets").get(Long.toString(retweetId), RetweetResponse.class);
        return retweetFromCache;
    }

    @Nullable
    private List<TweetResponse> getRepliesFromCache(long replyToId) {
        List<TweetResponse> repliesFromCache = cacheManager.getCache("repliesForTweet").get(Long.toString(replyToId), List.class);
        return repliesFromCache;
    }

    private Retweet buildDefaultRetweet(long retweetId, Tweet parentTweet) {
        Retweet retweet = Retweet.builder()
                .id(retweetId)
                .parentTweet(parentTweet)
                .retweetTime(LocalDateTime.now())
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
