package com.example.tweets.util;

import com.example.tweets.repository.LikeRepository;
import com.example.tweets.repository.RetweetRepository;
import com.example.tweets.repository.TweetRepository;
import com.example.tweets.repository.ViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TweetUtil {

    private final TweetRepository tweetRepository;
    private final LikeRepository likeRepository;
    private final RetweetRepository retweetRepository;
    private final ViewRepository viewRepository;

    public int countRepliesForTweet(Long tweetId) {
        return tweetRepository.countAllByReplyToId(tweetId);
    }

    public int countLikesForTweet(Long tweetId) {
        return likeRepository.countAllByParentTweetId(tweetId);
    }

    public int countRetweetsForTweet(Long tweetId) {
        return retweetRepository.countAllByParentTweetId(tweetId);
    }

    public int countViewsForTweet(Long tweetId) {
        return viewRepository.countAllByParentTweetId(tweetId);
    }
}
