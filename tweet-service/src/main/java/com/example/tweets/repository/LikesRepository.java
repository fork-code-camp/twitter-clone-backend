package com.example.tweets.repository;

import com.example.tweets.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikesRepository extends JpaRepository<Like, Long> {

    Long countAllByTweetId(Long tweetId);

    List<Like> findByProfileId(String profileId);

    List<Like> findByTweetId(Long tweetId);

    Optional<Like> findByProfileIdAndTweetId(String profileId, Long tweetId);
}
