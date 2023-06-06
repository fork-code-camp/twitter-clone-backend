package com.example.tweet.repository;

import com.example.tweet.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByProfileIdAndParentTweetId(String profileId, Long parentTweetId);

    Integer countAllByParentTweetId(Long parentTweetId);
}
