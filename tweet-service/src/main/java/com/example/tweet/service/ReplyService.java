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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.example.tweet.constant.CacheName.REPLIES_CACHE_NAME;
import static com.example.tweet.constant.CacheName.REPLIES_FOR_TWEET_CACHE_NAME;
import static com.example.tweet.constant.Operation.*;

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
    private final CacheManager cacheManager;

    public TweetResponse reply(TweetCreateRequest request, Long replyToId, String loggedInUser, MultipartFile[] files) {
        return tweetRepository.findById(replyToId)
                .map(replyTo -> tweetMapper.toEntity(request, null, replyTo, profileServiceClient, loggedInUser))
                .map(reply -> mediaUtil.addMedia(reply, files))
                .map(tweetRepository::saveAndFlush)
                .map(reply -> {
                    tweetUtil.sendMessageWithReply(reply, ADD);
                    return reply;
                })
                .map(reply -> tweetMapper.toResponse(reply, loggedInUser, tweetUtil, profileServiceClient))
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", replyToId)
                ));
    }

    @CacheEvict(cacheNames = REPLIES_CACHE_NAME, key = "#p0")
    public boolean deleteReply(Long replyId, String loggedInUser) {
        tweetRepository.findById(replyId)
                .filter(reply -> tweetUtil.isEntityOwnedByLoggedInUser(reply, loggedInUser))
                .ifPresentOrElse(reply -> {
                    Objects.requireNonNull(cacheManager.getCache(REPLIES_FOR_TWEET_CACHE_NAME)).evictIfPresent(Long.toString(reply.getReplyTo().getId()));
                    tweetRepository.delete(reply);
                    tweetUtil.sendMessageWithReply(reply, DELETE);
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
                    Objects.requireNonNull(cacheManager.getCache(REPLIES_FOR_TWEET_CACHE_NAME)).evictIfPresent(Long.toString(reply.getReplyTo().getId()));
                    return reply;
                })
                .map(reply -> tweetMapper.toResponse(reply, loggedInUser, tweetUtil, profileServiceClient))
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSourceService.generateMessage("error.entity.not_found", replyId)
                ));

    }

    @Cacheable(cacheNames = REPLIES_CACHE_NAME, key = "#p0")
    public TweetResponse getReply(Long replyId, String loggedInUser) {
        return tweetRepository.findByIdAndReplyToIsNotNull(replyId)
                .map(reply -> viewService.createViewEntity(reply, loggedInUser, profileServiceClient))
                .map(reply -> tweetMapper.toResponse(reply, loggedInUser, tweetUtil, profileServiceClient))
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

    @Cacheable(cacheNames = REPLIES_FOR_TWEET_CACHE_NAME, key = "#p0", unless = "#result.size() < 100")
    public List<TweetResponse> getAllRepliesForTweet(Long replyToId, String loggedInUser) {
        return tweetRepository.findAllByReplyToIdOrderByCreationDateDesc(replyToId)
                .stream()
                .map(reply -> tweetMapper.toResponse(reply, loggedInUser, tweetUtil, profileServiceClient))
                .collect(Collectors.toList());
    }
}
