package com.example.tweet.repository;

import com.example.tweet.entity.Tweet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TweetRepository extends JpaRepository<Tweet, Long> {

    List<Tweet> findAllByProfileIdAndReplyToIsNullOrderByCreationDateDesc(String profileId);

    List<Tweet> findAllByProfileIdAndReplyToIsNotNullOrderByCreationDateDesc(String profileId);

    List<Tweet> findAllByReplyToId(Long replyToId);

    Integer countAllByReplyToId(Long replyToId);
}
