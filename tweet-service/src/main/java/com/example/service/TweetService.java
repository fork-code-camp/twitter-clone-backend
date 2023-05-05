package com.example.service;

import com.example.dto.request.TweetCreateRequest;
import com.example.dto.request.TweetUpdateRequest;
import com.example.dto.response.TweetResponse;
import com.example.entity.Tweet;
import com.example.exception.CreateEntityException;
import com.example.mapper.TweetMapper;
import com.example.repository.TweetRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TweetService {

    private final LikeService likeService;
    private final TweetRepository tweetRepository;
    private final ProfileClientService profileClientService;
    private final TweetMapper tweetMapper;
    private final MessageSourceService messageSourceService;

    public TweetResponse postTweet(TweetCreateRequest tweetCreateRequest, HttpServletRequest httpServletRequest) {
        return Optional.of(tweetCreateRequest)
                .map(tweetMapper::toEntity)
                .map(tweet -> buildResponse(tweet, httpServletRequest))
                .orElseThrow(() -> new CreateEntityException(
                        messageSourceService.generateMessage("error.unsuccessful_creation")
                ));
    }

    public TweetResponse getTweet(Long id, HttpServletRequest httpServletRequest) {
        return tweetRepository.findById(id)
                .map(tweet -> buildResponse(tweet, httpServletRequest))
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", id)
                ));
    }

    public void deleteTweet(Long id) {
        tweetRepository.findById(id)
                .map(tweet -> {
                    tweetRepository.delete(tweet);
                    return tweet;
                })
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", id)
                ));
    }

    public TweetResponse updateTweet(Long id, TweetUpdateRequest tweetUpdateRequest, HttpServletRequest httpServletRequest) {
        return tweetRepository.findById(id)
                .map(tweet -> {
                    tweetMapper.updateTweet(tweetUpdateRequest, tweet);
                    tweetRepository.saveAndFlush(tweet);
                    return tweet;
                })
                .map(tweet -> buildResponse(tweet, httpServletRequest))
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", id)
                ));
    }

    public String getProfileIdFromTweet(Long id) {
        return tweetRepository.findById(id)
                .map(Tweet::getProfileId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", id)
                ));
    }

    public Tweet getTweetEntity(Long id) {
        return tweetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", id)
                ));
    }

    private TweetResponse buildResponse(Tweet tweet, HttpServletRequest httpServletRequest) {
        TweetResponse tweetResponse = tweetMapper.toResponse(tweet);

        if (tweetResponse.getCreationDate() == null) tweetResponse.setCreationDate(LocalDateTime.now());

        Long likes = likeService.getLikesForTweetById(tweet.getId());
        String profileUsername = profileClientService.getProfileUsername(httpServletRequest);

        tweetResponse.setLikes(likes);
        tweetResponse.setUsername(profileUsername);

        return tweetResponse;
    }
}
