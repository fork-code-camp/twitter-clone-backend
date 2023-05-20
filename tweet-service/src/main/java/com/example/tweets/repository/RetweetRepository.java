package com.example.tweets.repository;

import com.example.tweets.entity.Retweet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RetweetRepository extends JpaRepository<Retweet, Long> {

    Integer countAllByParentTweetId(Long id);
    Optional<Retweet> findByParentTweetIdAndProfileId(Long originalTweetId, String profileId);
    List<Retweet> findAllByProfileId(String profileId);
}
