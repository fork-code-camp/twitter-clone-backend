package com.example.tweet.repository;

import com.example.tweet.entity.Tweet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TweetRepository extends JpaRepository<Tweet, Long> {

    List<Tweet> findAllByProfileIdAndReplyToIsNullAndRetweetToIsNullOrderByCreationDateDesc(String profileId);

    List<Tweet> findAllByProfileIdAndReplyToIsNotNullOrderByCreationDateDesc(String profileId);

    List<Tweet> findAllByProfileIdAndRetweetToIsNotNullOrderByCreationDateDesc(String profileId);

    List<Tweet> findAllByReplyToIdOrderByCreationDateDesc(Long replyToId);

    Optional<Tweet> findByProfileIdAndRetweetToId(String profileId, Long retweetToId);

    Integer countAllByReplyToId(Long replyToId);

    Integer countAllByRetweetToId(Long retweetToId);
}
