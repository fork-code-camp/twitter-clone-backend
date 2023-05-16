package com.example.tweets.repository;

import com.example.tweets.entity.Tweet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RetweetRepository extends JpaRepository<Tweet, Long> {

    Integer countAllByOriginalTweetId(Long id);

    Optional<Tweet> findByOriginalTweetIdAndProfileId(Long id, String profileId);

    List<Tweet> findAllByProfileIdAndOriginalTweetNotNull(String profileId);
}
