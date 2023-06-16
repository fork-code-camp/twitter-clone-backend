package com.example.tweet.util;

import com.example.tweet.repository.LikeRepository;
import com.example.tweet.repository.TweetRepository;
import com.example.tweet.repository.ViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TweetUtil {

    private final TweetRepository tweetRepository;
    private final LikeRepository likeRepository;
    private final ViewRepository viewRepository;

    public int countRepliesForTweet(Long tweetId) {
        return tweetRepository.countAllByReplyToId(tweetId);
    }

    public int countLikesForTweet(Long tweetId) {
        return likeRepository.countAllByParentTweetId(tweetId);
    }

    public int countRetweetsForTweet(Long tweetId) {
        return tweetRepository.countAllByRetweetToId(tweetId);
    }

    public int countViewsForTweet(Long tweetId) {
        return viewRepository.countAllByParentTweetId(tweetId);
    }
}
