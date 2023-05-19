package com.example.tweets.repository;

import com.example.tweets.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikesRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByProfileIdAndParentTweetId(String profileId, Long tweetId);
}
