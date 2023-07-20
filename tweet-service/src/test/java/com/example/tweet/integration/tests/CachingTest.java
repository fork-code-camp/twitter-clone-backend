package com.example.tweet.integration.tests;


import com.example.tweet.constant.EntityName;
import com.example.tweet.constant.Operation;
import com.example.tweet.dto.request.TweetUpdateRequest;
import com.example.tweet.dto.response.TweetResponse;
import com.example.tweet.entity.Tweet;
import com.example.tweet.integration.IntegrationTestBase;
import com.example.tweet.repository.TweetRepository;
import com.example.tweet.service.ReplyService;
import com.example.tweet.service.RetweetService;
import com.example.tweet.service.TweetService;
import com.example.tweet.service.ViewService;
import com.example.tweet.util.TweetUtil;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
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
@SuppressWarnings("SameParameterValue")
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

    @Test
    public void cacheTweetTest() {
        Tweet tweet = createStubForTweet(1L, false);

        tweetService.getTweet(tweet.getId(), EMAIL.getConstant());
        TweetResponse tweetResponse = tweetService.getTweet(tweet.getId(), EMAIL.getConstant());

        verify(tweetRepository, times(1)).findById(tweet.getId());

        TweetResponse tweetFromCache = getEntityFromCache(tweet.getId(), TWEETS_CACHE_NAME);
        assertNotNull(tweetFromCache);
        assertEquals(tweetResponse, tweetFromCache);
    }

    @Test
    public void updateTweetInCacheTest() {
        Tweet tweet = createStubForTweet(1L, false);

        tweetService.getTweet(tweet.getId(), EMAIL.getConstant());
        TweetResponse tweetFromCache = getEntityFromCache(tweet.getId(), TWEETS_CACHE_NAME);
        assertNotNull(tweetFromCache);

        TweetResponse tweetResponse = tweetService.updateTweet(
                tweet.getId(),
                new TweetUpdateRequest(UPDATE_TWEET_TEXT.getConstant()),
                EMAIL.getConstant(),
                null
        );
        TweetResponse updatedTweetFromCache = getEntityFromCache(tweet.getId(), TWEETS_CACHE_NAME);
        assertEquals(tweetResponse, updatedTweetFromCache);
    }

    @Test
    public void deleteTweetFromCacheTest() {
        Tweet tweet = createStubForTweet(1L, false);

        tweetService.getTweet(tweet.getId(), EMAIL.getConstant());
        TweetResponse tweetFromCache = getEntityFromCache(tweet.getId(), TWEETS_CACHE_NAME);
        assertNotNull(tweetFromCache);

        tweetService.deleteTweet(tweet.getId(), EMAIL.getConstant());
        TweetResponse deletedTweetFromCache = getEntityFromCache(tweet.getId(), TWEETS_CACHE_NAME);
        assertNull(deletedTweetFromCache);
    }

    @Test
    public void cacheRepliesForTweetTest() {
        Tweet tweet = createStubForTweetWithReplies(1L, 100);

        replyService.getAllRepliesForTweet(tweet.getId(), EMAIL.getConstant());
        List<TweetResponse> repliesFromCache = getEntitiesFromCache(tweet.getId(), REPLIES_FOR_TWEET_CACHE_NAME);
        assertNotNull(repliesFromCache);
        assertEquals(100, repliesFromCache.size());
    }

    @Test
    public void cacheReplyTest() {
        Tweet reply = createStubForReply(1L, 2L);

        TweetResponse replyResponse = replyService.getReply(reply.getId(), EMAIL.getConstant());
        TweetResponse replyFromCache = getEntityFromCache(reply.getId(), REPLIES_CACHE_NAME);
        assertNotNull(replyFromCache);
        assertEquals(replyResponse, replyFromCache);
    }

    @Test
    public void updateReplyInCacheTest() {
        Tweet reply = createStubForReply(1L, 2L);
        replyService.getReply(reply.getId(), EMAIL.getConstant());
        assertNotNull(getEntityFromCache(reply.getId(), REPLIES_CACHE_NAME));

        TweetResponse replyResponse = replyService.updateReply(
                reply.getId(),
                new TweetUpdateRequest(UPDATE_REPLY_TEXT.getConstant()), EMAIL.getConstant(),
                null
        );
        TweetResponse replyFromCache = getEntityFromCache(reply.getId(), REPLIES_CACHE_NAME);
        assertNotNull(replyFromCache);
        assertEquals(replyResponse, replyFromCache);
    }

    @Test
    public void deleteReplyFromCacheTest() {
        Tweet tweetWithReplies = createStubForTweetWithReplies(1L, 100);
        Tweet reply = createStubForReply(tweetWithReplies.getId(), 2L);
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
        Tweet retweet = createStubForRetweet(1L, 2L);

        retweetService.getRetweetById(retweet.getId(), EMAIL.getConstant());
        TweetResponse retweetResponse = retweetService.getRetweetById(retweet.getId(), EMAIL.getConstant());

        verify(tweetRepository, times(1)).findByIdAndRetweetToIsNotNull(retweet.getId());

        TweetResponse retweetFromCache = getEntityFromCache(retweet.getId(), RETWEETS_CACHE_NAME);
        assertNotNull(retweetFromCache);
        assertEquals(retweetResponse, retweetFromCache);
    }

    @Test
    public void deleteRetweetFromCache() {
        Tweet retweet = createStubForRetweet(1L, 2L);

        retweetService.getRetweetById(retweet.getId(), EMAIL.getConstant());
        TweetResponse retweetFromCache = getEntityFromCache(retweet.getId(), RETWEETS_CACHE_NAME);
        assertNotNull(retweetFromCache);

        retweetService.undoRetweet(retweet.getRetweetTo().getId(), EMAIL.getConstant());
        TweetResponse deletedRetweetFromCache = getEntityFromCache(retweet.getId(), RETWEETS_CACHE_NAME);
        assertNull(deletedRetweetFromCache);
    }

    @SuppressWarnings({"unchecked", "DataFlowIssue"})
    private Tweet createStubForTweetWithReplies(long tweetId, int replies) {
        Tweet parentTweet = createStubForTweet(tweetId, false);

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

    private Tweet createStubForReply(long replyToId, long replyId) {
        Tweet replyTo = createStubForTweet(replyToId, false);
        Tweet reply = buildDefaultReply(replyId, replyTo);

        when(tweetRepository.findByIdAndReplyToIsNotNull(replyId))
                .thenReturn(Optional.of(reply));

        when(viewService.createViewEntity(eq(reply), anyString(), any()))
                .thenReturn(reply);

        mockBasicThings(reply, replyId, false);

        return reply;
    }

    private Tweet createStubForRetweet(long retweetToId, long retweetId) {
        Tweet retweetTo = createStubForTweet(retweetToId, true);
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

    private Tweet createStubForTweet(long tweetId, boolean isRetweeted) {
        Tweet tweet = buildDefaultTweet(tweetId);

        when(viewService.createViewEntity(eq(tweet), anyString(), any()))
                .thenReturn(tweet);

        mockBasicThings(tweet, tweetId, isRetweeted);

        return tweet;
    }

    private void mockBasicThings(Tweet entity, long entityId, boolean isRetweeted) {
        when(tweetRepository.findById(entityId))
                .thenReturn(Optional.of(entity));

        when(tweetRepository.saveAndFlush(entity))
                .thenReturn(entity);

        doReturn(isRetweeted)
                .when(tweetUtil).isTweetRetweetedByLoggedInUser(eq(entityId), anyString(), any());

        doReturn(false)
                .when(tweetUtil).isTweetLikedByLoggedInUser(eq(entityId), anyString(), any());

        doNothing()
                .when(tweetUtil).sendMessageToKafka(anyString(), any(Tweet.class), any(EntityName.class), any(Operation.class));
    }

    @Nullable
    @SuppressWarnings("DataFlowIssue")
    private TweetResponse getEntityFromCache(long entityId, String cacheName) {
        return cacheManager.getCache(cacheName).get(Long.toString(entityId), TweetResponse.class);
    }

    @Nullable
    @SuppressWarnings({"DataFlowIssue", "unchecked"})
    private List<TweetResponse> getEntitiesFromCache(long parentEntityId, String cacheName) {
        return cacheManager.getCache(cacheName).get(Long.toString(parentEntityId), List.class);
    }

    private Tweet buildDefaultReply(long replyId, Tweet replyTo) {
        return Tweet.builder()
                .id(replyId)
                .text(DEFAULT_REPLY_TEXT.getConstant())
                .replyTo(replyTo)
                .creationDate(LocalDateTime.MAX)
                .profileId(ID.getConstant())
                .build();
    }

    private Tweet buildDefaultRetweet(long retweetId, Tweet retweetTo) {
        return Tweet.builder()
                .id(retweetId)
                .retweetTo(retweetTo)
                .creationDate(LocalDateTime.MAX)
                .profileId(ID.getConstant())
                .build();
    }

    private Tweet buildDefaultTweet(long tweetId) {
        return Tweet.builder()
                .id(tweetId)
                .text(DEFAULT_TWEET_TEXT.getConstant())
                .creationDate(LocalDateTime.MAX)
                .profileId(ID.getConstant())
                .build();
    }
}
