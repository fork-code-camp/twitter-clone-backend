package com.example.tweets.repository;

import com.example.tweets.entity.Tweet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TweetRepository extends JpaRepository<Tweet, Long> {

    Integer countAllByParentTweetForReplyId(Long parentTweetForReplyId);

    List<Tweet> findAllByParentTweetForReplyIsNull();

    List<Tweet> findAllByParentTweetForReplyId(Long parentTweetId);

    List<Tweet> findAllByProfileIdAndParentTweetForReplyIsNull(String profileId);

    List<Tweet> findAllByProfileIdAndParentTweetForReplyIsNotNull(String profileId);
}
