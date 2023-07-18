package com.example.tweet.integration.tests;


import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.client.StorageServiceClient;
import com.example.tweet.constant.EntityName;
import com.example.tweet.constant.Operation;
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
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.example.tweet.constant.CacheName.*;
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

    @SpyBean
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
        cacheManager.getCache("replies").clear();
        cacheManager.getCache("repliesForTweet").clear();
    }

    @Test
    public void cacheTweetTest() {
        createStubForTweet(1L, 25000, 5000, 1000, 1000, false);

        tweetService.getTweet(1L, EMAIL.getConstant());
        tweetService.getTweet(1L, EMAIL.getConstant());

        verify(tweetRepository, times(1))
                .findById(1L);

        TweetResponse tweetFromCache = getEntityFromCache(1L, TWEETS_CACHE_NAME);
        assertNotNull(tweetFromCache);
        assertEquals(25000, tweetFromCache.getViews());
        assertEquals(5000, tweetFromCache.getLikes());
        assertEquals(1000, tweetFromCache.getRetweets());
        assertEquals(1000, tweetFromCache.getReplies());
    }

    @Test
    public void doNotCacheTweetTest() {
        createStubForTweet(1L, 1000, 250, 100, 100, false);

        tweetService.getTweet(1L, EMAIL.getConstant());
        tweetService.getTweet(1L, EMAIL.getConstant());

        verify(tweetRepository, times(2)).findById(1L);

        TweetResponse tweetFromCache = getEntityFromCache(1L, TWEETS_CACHE_NAME);
        assertNull(tweetFromCache);
    }

    @Test
    public void updateTweetInCacheTest() {
        createStubForTweet(1L, 25000, 5000, 1000, 1000, false);

        tweetService.getTweet(1L, EMAIL.getConstant());
        TweetResponse tweetFromCache = getEntityFromCache(1L, TWEETS_CACHE_NAME);
        assertEquals(DEFAULT_TWEET_TEXT.getConstant(), tweetFromCache.getText());

        tweetService.updateTweet(1L, new TweetUpdateRequest(UPDATE_TWEET_TEXT.getConstant()), EMAIL.getConstant(), null);
        TweetResponse updatedTweetFromCache = getEntityFromCache(1L, TWEETS_CACHE_NAME);
        assertEquals(UPDATE_TWEET_TEXT.getConstant(), updatedTweetFromCache.getText());
    }

    @Test
    public void deleteTweetFromCacheTest() {
        createStubForTweet(1L, 25000, 5000, 1000, 1000, false);

        tweetService.getTweet(1L, EMAIL.getConstant());
        TweetResponse tweetFromCache = getEntityFromCache(1L, TWEETS_CACHE_NAME);
        assertNotNull(tweetFromCache);

        tweetService.deleteTweet(1L, EMAIL.getConstant());
        TweetResponse deletedTweetFromCache = getEntityFromCache(1L, TWEETS_CACHE_NAME);
        assertNull(deletedTweetFromCache);
    }

    @Test
    public void cacheRepliesForTweetTest() {
        Tweet tweetWithReplies = createStubForTweetWithReplies(1L, 0, 0, 0, 1000);

        replyService.getAllRepliesForTweet(1L, EMAIL.getConstant());
        List<TweetResponse> repliesFromCache = getEntitiesFromCache(1L, REPLIES_FOR_TWEET_CACHE_NAME);
        assertNotNull(repliesFromCache);
        assertEquals(1000, repliesFromCache.size());
    }

    @Test
    public void cacheReplyTest() {
        Tweet reply = createStubForReply(1L, 2L, 2500, 500, 100, 100, 1000);

        replyService.getReply(reply.getId(), EMAIL.getConstant());
        TweetResponse replyFromCache = getEntityFromCache(reply.getId(), REPLIES_CACHE_NAME);
        assertNotNull(replyFromCache);
        assertEquals(reply.getId(), replyFromCache.getId());
        assertEquals(reply.getText(), replyFromCache.getText());
    }

    @Test
    public void updateReplyInCacheTest() {
        Tweet reply = createStubForReply(1L, 2L, 2500, 500, 100, 100, 1000);
        replyService.getReply(reply.getId(), EMAIL.getConstant());
        assertNotNull(getEntityFromCache(reply.getId(), REPLIES_CACHE_NAME));

        replyService.updateReply(reply.getId(), new TweetUpdateRequest(UPDATE_REPLY_TEXT.getConstant()), EMAIL.getConstant(), null);
        TweetResponse replyFromCache = getEntityFromCache(reply.getId(), REPLIES_CACHE_NAME);
        assertNotNull(replyFromCache);
        assertEquals(UPDATE_REPLY_TEXT.getConstant(), replyFromCache.getText());
    }

    @Test
    public void deleteReplyFromCacheTest() {
        Tweet tweetWithReplies = createStubForTweetWithReplies(1L, 0, 0, 0, 1000);
        Tweet reply = createStubForReply(tweetWithReplies.getId(), 2L, 0, 0, 0, 0, 1000);
        replyService.getAllRepliesForTweet(tweetWithReplies.getId(), EMAIL.getConstant());
        replyService.getReply(reply.getId(), EMAIL.getConstant());

        replyService.deleteReply(reply.getId(), EMAIL.getConstant());
        List<TweetResponse> repliesFromCache = getEntitiesFromCache(tweetWithReplies.getId(), REPLIES_FOR_TWEET_CACHE_NAME);
        TweetResponse replyFromCache = getEntityFromCache(reply.getId(), REPLIES_CACHE_NAME);
        assertNull(repliesFromCache);
        assertNull(replyFromCache);
    }

    @Test
    public void cacheRetweetTest() {
        createStubForRetweet(1L, 2L, 25000, 5000, 1000, 1000);

        retweetService.getRetweetById(2L, EMAIL.getConstant());
        TweetResponse retweet = retweetService.getRetweetById(2L, EMAIL.getConstant());

        verify(tweetRepository, times(1)).findByIdAndRetweetToIsNotNull(2L);

        TweetResponse retweetFromCache = getEntityFromCache(2L, RETWEETS_CACHE_NAME);
        assertNotNull(retweetFromCache);
        assertEquals(retweet, retweetFromCache);
    }

    @Test
    public void deleteRetweetFromCache() {
        createStubForRetweet(1L, 2L, 25000, 5000, 1000, 1000);

        retweetService.getRetweetById(2L, EMAIL.getConstant());
        TweetResponse retweetFromCache = getEntityFromCache(2L, RETWEETS_CACHE_NAME);
        assertNotNull(retweetFromCache);

        retweetService.undoRetweet(1L, EMAIL.getConstant());
        TweetResponse deletedRetweetFromCache = getEntityFromCache(2L, RETWEETS_CACHE_NAME);
        assertNull(deletedRetweetFromCache);
    }

    private Tweet createStubForTweetWithReplies(long tweetId, int views, int likes, int retweets, int replies) {
        Tweet parentTweet = createStubForTweet(tweetId, views, likes, retweets, replies, false);

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

    private Tweet createStubForReply(long replyToId, long replyId, int views, int likes, int retweets, int replies, int tweetReplies) {
        Tweet replyTo = createStubForTweet(replyToId, 0, 0, 0, tweetReplies, false);
        Tweet reply = buildDefaultReply(replyId, replyTo);

        when(tweetRepository.findByIdAndReplyToIsNotNull(replyId))
                .thenReturn(Optional.of(reply));

        when(viewService.createViewEntity(eq(reply), anyString(), any()))
                .thenReturn(reply);

        mockBasicThings(reply, replyId, views, likes, retweets, replies, false);

        return reply;
    }

    private Tweet createStubForRetweet(long retweetToId, long retweetId, int views, int likes, int retweets, int replies) {
        Tweet retweetTo = createStubForTweet(retweetToId, views, likes, retweets, replies, true);
        Tweet retweet = buildDefaultRetweet(retweetId, retweetTo);

        when(tweetRepository.findByIdAndRetweetToIsNotNull(retweetId))
                .thenReturn(Optional.of(retweet));

        when(tweetRepository.findByRetweetToIdAndProfileId(retweetToId, ID.getConstant()))
                .thenReturn(Optional.of(retweet));

        doNothing()
                .when(tweetRepository)
                .delete(retweet);

        return retweet;
    }

    private Tweet createStubForTweet(long tweetId, int views, int likes, int retweets, int replies, boolean isRetweeted) {
        Tweet tweet = buildDefaultTweet(tweetId);

        when(viewService.createViewEntity(eq(tweet), anyString(), any()))
                .thenReturn(tweet);

        mockBasicThings(tweet, tweetId, views, likes, retweets, replies, isRetweeted);

        return tweet;
    }

    private void mockBasicThings(Tweet entity, long entityId, int views, int likes, int retweets, int replies, boolean isRetweeted) {
        when(tweetRepository.findById(entityId))
                .thenReturn(Optional.of(entity));

        when(tweetRepository.saveAndFlush(entity))
                .thenReturn(entity);

        when(tweetUtil.countViewsForTweet(entityId))
                .thenReturn(views);

        when(tweetUtil.countLikesForTweet(entityId))
                .thenReturn(likes);

        when(tweetUtil.countRetweetsForTweet(entityId))
                .thenReturn(retweets);

        when(tweetUtil.countRepliesForTweet(entityId))
                .thenReturn(replies);

        doReturn(isRetweeted)
                .when(tweetUtil).isTweetRetweetedByLoggedInUser(eq(entityId), anyString(), any());

        doReturn(false)
                .when(tweetUtil).isTweetLikedByLoggedInUser(eq(entityId), anyString(), any());

        doNothing()
                .when(tweetUtil).sendMessageToKafka(anyString(), any(TweetResponse.class), any(EntityName.class), any(Operation.class));
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

    private Tweet buildDefaultReply(long replyId, Tweet replyTo) {
        return Tweet.builder()
                .id(replyId)
                .text(DEFAULT_REPLY_TEXT.getConstant())
                .replyTo(replyTo)
                .creationDate(LocalDateTime.now())
                .profileId(ID.getConstant())
                .build();
    }

    private Tweet buildDefaultRetweet(long retweetId, Tweet retweetTo) {
        return Tweet.builder()
                .id(retweetId)
                .retweetTo(retweetTo)
                .creationDate(LocalDateTime.now())
                .profileId(ID.getConstant())
                .build();
    }

    private Tweet buildDefaultTweet(long tweetId) {
        return Tweet.builder()
                .id(tweetId)
                .text(DEFAULT_TWEET_TEXT.getConstant())
                .creationDate(LocalDateTime.now())
                .profileId(ID.getConstant())
                .build();
    }
}
