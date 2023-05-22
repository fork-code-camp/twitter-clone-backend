package com.example.tweets.repository;

import com.example.tweets.entity.Tweet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TweetRepository extends JpaRepository<Tweet, Long> {

    List<Tweet> findAllByReplyToIsNull();

    List<Tweet> findAllByProfileIdAndReplyToIsNull(String profileId);

    List<Tweet> findAllByProfileIdAndReplyToIsNotNull(String profileId);

    Integer countAllByReplyToId(Long replyToId);
}
