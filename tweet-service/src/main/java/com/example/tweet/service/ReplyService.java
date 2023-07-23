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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.example.tweet.constant.CacheName.REPLIES_CACHE_NAME;
import static com.example.tweet.constant.CacheName.REPLIES_FOR_TWEET_CACHE_NAME;
import static com.example.tweet.constant.Operation.ADD;
import static com.example.tweet.constant.Operation.DELETE;
import static com.example.tweet.util.TweetUtil.EvictionStrategy.CACHE_ONLY;
import static com.example.tweet.util.TweetUtil.EvictionStrategy.WITH_TIMELINE;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final TweetRepository tweetRepository;
    private final TweetUtil tweetUtil;
    private final MediaUtil mediaUtil;
    private final TweetMapper tweetMapper;
    private final ProfileServiceClient profileServiceClient;
    private final MessageSourceService messageSourceService;
    private final ViewService viewService;
    private final TweetService tweetService;
    private final CacheManager cacheManager;

    public TweetResponse reply(TweetCreateRequest request, Long replyToId, String loggedInUser, MultipartFile[] files) {
        return tweetRepository.findById(replyToId)
                .map(replyTo -> tweetMapper.toEntity(request, null, replyTo, profileServiceClient, loggedInUser))
                .map(reply -> mediaUtil.addMedia(reply, files))
                .map(tweetRepository::saveAndFlush)
                .map(reply -> {
                    tweetUtil.evictEntityFromCache(reply.getReplyTo().getId(), REPLIES_FOR_TWEET_CACHE_NAME);
                    tweetUtil.sendMessageWithReply(reply, ADD);
                    return tweetMapper.toResponse(reply, loggedInUser, tweetUtil, profileServiceClient);
                })
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", replyToId)
                ));
    }

    @CacheEvict(cacheNames = REPLIES_CACHE_NAME, key = "#p0")
    public boolean deleteReply(Long replyId, String loggedInUser) {
        tweetRepository.findById(replyId)
                .filter(reply -> tweetUtil.isEntityOwnedByLoggedInUser(reply, loggedInUser))
                .ifPresentOrElse(reply -> {
                    tweetUtil.evictEntityFromCache(reply.getReplyTo().getId(), REPLIES_FOR_TWEET_CACHE_NAME);
                    tweetUtil.evictAllEntityRelationsFromCache(reply, WITH_TIMELINE);
                    tweetUtil.sendMessageWithReply(reply, DELETE);
                    tweetRepository.delete(reply);
                }, () -> {
                    throw new EntityNotFoundException(
                            messageSourceService.generateMessage("error.entity.not_found", replyId)
                    );
                });
        return true;
    }

    @CachePut(cacheNames = REPLIES_CACHE_NAME, key = "#p0")
    public TweetResponse updateReply(Long replyId, TweetUpdateRequest request, String loggedInUser, MultipartFile[] files) {
        return tweetRepository.findById(replyId)
                .filter(reply -> tweetUtil.isEntityOwnedByLoggedInUser(reply, loggedInUser))
                .map(reply -> tweetMapper.updateTweet(request, reply))
                .map(reply -> mediaUtil.updateMedia(reply, files))
                .map(tweetRepository::saveAndFlush)
                .map(reply -> {
                    tweetUtil.evictEntityFromCache(reply.getReplyTo().getId(), REPLIES_FOR_TWEET_CACHE_NAME);
                    tweetUtil.evictAllEntityRelationsFromCache(reply, CACHE_ONLY);
                    return tweetMapper.toResponse(reply, loggedInUser, tweetUtil, profileServiceClient);
                })
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", replyId)
                ));

    }

    public TweetResponse getReplyById(Long replyId, String loggedInUser) {
        Cache cache = Objects.requireNonNull(cacheManager.getCache(REPLIES_CACHE_NAME));
        TweetResponse replyResponse = cache.get(replyId, TweetResponse.class);
        if (replyResponse != null) {
            return updateReplyResponse(tweetUtil.updateProfileInResponse(replyResponse));
        }
        return tweetRepository.findByIdAndReplyToIsNotNull(replyId)
                .map(reply -> viewService.createViewEntity(reply, loggedInUser, profileServiceClient))
                .map(reply -> {
                    TweetResponse response = tweetMapper.toResponse(reply, loggedInUser, tweetUtil, profileServiceClient);
                    cache.put(replyId, response);
                    return response;
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

    @SuppressWarnings("unchecked")
    public List<TweetResponse> getAllRepliesForTweet(Long replyToId, String loggedInUser) {
        Cache cache = Objects.requireNonNull(cacheManager.getCache(REPLIES_FOR_TWEET_CACHE_NAME));
        List<TweetResponse> replyResponses = cache.get(replyToId, List.class);
        if (replyResponses != null) {
            return replyResponses.stream()
                    .map(tweetUtil::updateProfileInResponse)
                    .map(this::updateReplyResponse)
                    .collect(Collectors.toList());
        }

        replyResponses = tweetRepository.findAllByReplyToIdOrderByCreationDateDesc(replyToId)
                .stream()
                .map(reply -> tweetMapper.toResponse(reply, loggedInUser, tweetUtil, profileServiceClient))
                .collect(Collectors.toList());
        cache.put(replyToId, replyResponses);
        return replyResponses;
    }

    private TweetResponse updateReplyResponse(TweetResponse replyResponse) {
        replyResponse.setReplyTo(tweetService.getTweetById(
                replyResponse.getReplyTo().getId(),
                replyResponse.getProfile().getEmail()
        ));
        return replyResponse;
    }
}
