package com.example.tweet.constant;

import org.springframework.stereotype.Component;

@Component
public class CacheName {

    public static final String TWEETS_CACHE_NAME = "tweets";
    public static final String RETWEETS_CACHE_NAME = "retweets";
    public static final String REPLIES_CACHE_NAME = "replies";
    public static final String REPLIES_FOR_TWEET_CACHE_NAME = "repliesForTweet";
}
