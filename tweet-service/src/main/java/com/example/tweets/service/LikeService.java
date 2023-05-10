package com.example.tweets.service;

import com.example.tweets.mapper.TweetMapper;
import com.example.tweets.repository.LikesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikesRepository likesRepository;
    private final TweetService tweetService;
    private final TweetMapper tweetMapper;

    public Long getLikesForTweetById(Long tweetId) {
        return likesRepository.countAllByTweetId(tweetId);
    }
}
