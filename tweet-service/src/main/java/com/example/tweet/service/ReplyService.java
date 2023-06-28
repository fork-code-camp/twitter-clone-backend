package com.example.tweet.service;

import com.example.tweet.client.ProfileServiceClient;
import com.example.tweet.dto.request.TweetCreateRequest;
import com.example.tweet.dto.request.TweetUpdateRequest;
import com.example.tweet.dto.response.ProfileResponse;
import com.example.tweet.dto.response.TweetResponse;
import com.example.tweet.mapper.TweetMapper;
import com.example.tweet.repository.TweetRepository;
import com.example.tweet.util.MediaUtil;
import com.example.tweet.util.TweetUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.example.tweet.constants.CacheName.REPLIES_CACHE_NAME;
import static com.example.tweet.constants.EntityName.REPLIES;
import static com.example.tweet.constants.EntityName.TWEETS;
import static com.example.tweet.constants.Operation.*;
import static com.example.tweet.constants.TopicName.HOME_TIMELINE_TOPIC;
import static com.example.tweet.constants.TopicName.USER_TIMELINE_TOPIC;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final TweetRepository tweetRepository;
    private final TweetUtil tweetUtil;
    private final MediaUtil mediaUtil;
    private final TweetMapper tweetMapper;
    private final ProfileServiceClient profileServiceClient;
    private final MessageSourceService messageSourceService;
    private final CacheManager cacheManager;

    public TweetResponse reply(TweetCreateRequest request, Long replyToId, String loggedInUser, MultipartFile[] files) {
        return tweetRepository.findById(replyToId)
                .map(replyTo -> tweetMapper.toEntity(request, null, replyTo, profileServiceClient, loggedInUser))
                .map(reply -> mediaUtil.addMedia(reply, files))
                .map(tweetRepository::saveAndFlush)
                .map(reply -> tweetMapper.toResponse(reply, loggedInUser, tweetUtil, profileServiceClient))
                .map(reply -> {
                    tweetUtil.sendMessageToKafka(USER_TIMELINE_TOPIC, reply, REPLIES, ADD);
                    tweetUtil.sendMessageToKafka(USER_TIMELINE_TOPIC, reply.getReplyTo(), TWEETS, UPDATE);
                    tweetUtil.sendMessageToKafka(HOME_TIMELINE_TOPIC, reply.getReplyTo(), TWEETS, UPDATE);
                    return reply;
                })
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", replyToId)
                ));
    }

    public boolean deleteReply(Long replyId, String loggedInUser) {
        tweetRepository.findById(replyId)
                .ifPresentOrElse(reply -> {
                    Objects.requireNonNull(cacheManager.getCache(REPLIES_CACHE_NAME)).evictIfPresent(Long.toString(reply.getReplyTo().getId()));
                    tweetRepository.delete(reply);
                    TweetResponse replyResponse = tweetMapper.toResponse(reply, loggedInUser, tweetUtil, profileServiceClient);
                    tweetUtil.sendMessageToKafka(USER_TIMELINE_TOPIC, replyResponse, REPLIES, DELETE);
                    tweetUtil.sendMessageToKafka(USER_TIMELINE_TOPIC, replyResponse.getReplyTo(), TWEETS, UPDATE);
                    tweetUtil.sendMessageToKafka(HOME_TIMELINE_TOPIC, replyResponse.getReplyTo(), TWEETS, UPDATE);
                }, () -> {
                    throw new EntityNotFoundException(
                            messageSourceService.generateMessage("error.entity.not_found", replyId)
                    );
                });
        return true;
    }

    public TweetResponse updateReply(Long replyId, TweetUpdateRequest request, String loggedInUser, MultipartFile[] files) {
        return tweetRepository.findById(replyId)
                .filter(reply -> tweetUtil.isTweetOwnedByLoggedInUser(reply, loggedInUser, profileServiceClient, messageSourceService))
                .map(reply -> tweetMapper.updateTweet(request, reply))
                .map(reply -> mediaUtil.updateMedia(reply, files))
                .map(tweetRepository::saveAndFlush)
                .map(reply -> tweetMapper.toResponse(reply, loggedInUser, tweetUtil, profileServiceClient))
                .map(reply -> {
                    tweetUtil.sendMessageToKafka(USER_TIMELINE_TOPIC, reply, REPLIES, UPDATE);
                    tweetUtil.sendMessageToKafka(USER_TIMELINE_TOPIC, reply.getReplyTo(), TWEETS, UPDATE);
                    tweetUtil.sendMessageToKafka(HOME_TIMELINE_TOPIC, reply.getReplyTo(), TWEETS, UPDATE);
                    return reply;
                })
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", replyId)
                ));

    }

    public List<TweetResponse> getAllRepliesForUser(String profileId, PageRequest page) {
        ProfileResponse profile = profileServiceClient.getProfileById(profileId);
        return tweetRepository.findAllByProfileIdAndReplyToIsNotNullOrderByCreationDateDesc(profileId, page)
                .stream()
                .map(reply -> tweetMapper.toResponse(reply, profile.getEmail(), tweetUtil, profileServiceClient))
                .collect(Collectors.toList());
    }

    @Cacheable(cacheNames = REPLIES_CACHE_NAME, key = "#p0", unless = "#result.size() < 1000")
    public List<TweetResponse> getAllRepliesForTweet(Long replyToId, String loggedInUser) {
        return tweetRepository.findAllByReplyToIdOrderByCreationDateDesc(replyToId)
                .stream()
                .map(reply -> tweetMapper.toResponse(reply, loggedInUser, tweetUtil, profileServiceClient))
                .collect(Collectors.toList());
    }
}
