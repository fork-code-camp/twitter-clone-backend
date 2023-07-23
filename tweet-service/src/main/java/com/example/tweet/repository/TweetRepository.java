package com.example.tweet.repository;

import com.example.tweet.entity.Tweet;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TweetRepository extends JpaRepository<Tweet, Long> {

    List<Tweet> findAllByProfileIdAndReplyToIsNullAndRetweetToIsNullOrderByCreationDateDesc(String profileId, Pageable page);

    List<Tweet> findAllByProfileIdAndReplyToIsNotNullOrderByCreationDateDesc(String profileId, Pageable page);

    List<Tweet> findAllByProfileIdAndRetweetToIsNotNullOrderByCreationDateDesc(String profileId, Pageable page);

    List<Tweet> findAllByReplyToIdOrderByCreationDateDesc(Long replyToId);

    List<Tweet> findAllByQuoteToId(Long quoteToId);

    Optional<Tweet> findByIdAndRetweetToIsNotNull(Long retweetId);

    Optional<Tweet> findByIdAndReplyToIsNotNull(Long replyId);

    Optional<Tweet> findByRetweetToIdAndProfileId(Long retweetToId, String profileId);

    Integer countAllByReplyToId(Long replyToId);

    Integer countAllByRetweetToId(Long retweetToId);
}
