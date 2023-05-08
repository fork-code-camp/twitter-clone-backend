package com.example.service;

import com.example.mapper.TweetMapper;
import com.example.repository.LikesRepository;
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
