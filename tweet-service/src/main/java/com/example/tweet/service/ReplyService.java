package com.example.tweet.service;

import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.dto.request.TweetCreateRequest;
import com.example.tweet.dto.response.TweetResponse;
import com.example.tweet.entity.Tweet;
import com.example.tweet.exception.CreateEntityException;
import com.example.tweet.mapper.TweetMapper;
import com.example.tweet.repository.TweetRepository;
import com.example.tweet.util.MediaUtil;
import com.example.tweet.util.TweetUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.tweet.service.FanoutService.EntityName.REPLIES;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final TweetService tweetService;
    private final TweetRepository tweetRepository;
    private final TweetUtil tweetUtil;
    private final MediaUtil mediaUtil;
    private final TweetMapper tweetMapper;
    private final ProfileServiceClient profileServiceClient;
    private final MessageSourceService messageSourceService;
    private final FanoutService fanoutService;

    public TweetResponse reply(TweetCreateRequest request, Long replyToId, String loggedInUser, MultipartFile[] files) {
        Tweet replyTo = tweetService.getTweetEntityById(replyToId);
        return Optional.of(request)
                .map(req -> tweetMapper.toEntity(req, null, replyTo, profileServiceClient, loggedInUser))
                .map(reply -> mediaUtil.addMedia(reply, files))
                .map(tweetRepository::saveAndFlush)
                .map(reply -> tweetMapper.toResponse(reply, tweetUtil, profileServiceClient))
                .map(reply -> fanoutService.addToUserTimeline(reply, REPLIES))
                .orElseThrow(() -> new CreateEntityException(
                        messageSourceService.generateMessage("error.entity.unsuccessful_creation")
                ));
    }

    public List<TweetResponse> findAllRepliesForUser(String loggedInUser, PageRequest page) {
        String profileId = profileServiceClient.getProfileIdByLoggedInUser(loggedInUser);
        return tweetRepository.findAllByProfileIdAndReplyToIsNotNullOrderByCreationDateDesc(profileId, page)
                .stream()
                .map(reply -> tweetMapper.toResponse(reply, tweetUtil, profileServiceClient))
                .collect(Collectors.toList());
    }

    @Cacheable(cacheNames = "repliesForTweet", key = "#p0", unless = "#result.size() < 1000")
    public List<TweetResponse> findAllRepliesForTweet(Long replyToId) {
        return tweetRepository.findAllByReplyToIdOrderByCreationDateDesc(replyToId)
                .stream()
                .map(reply -> tweetMapper.toResponse(reply, tweetUtil, profileServiceClient))
                .collect(Collectors.toList());
    }
}
