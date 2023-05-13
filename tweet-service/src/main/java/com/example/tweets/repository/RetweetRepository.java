package com.example.tweets.repository;

import com.example.tweets.entity.Retweet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RetweetRepository extends JpaRepository<Retweet, Long> {

    List<Retweet> findAllByProfileId(String profileId);

    Optional<Retweet> findByProfileIdAndTweetId(String profileId, Long tweetId);
}
