package com.example.tweets.repository;

import com.example.tweets.entity.View;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ViewRepository extends JpaRepository<View, Long> {

    Optional<View> findByProfileIdAndParentTweetId(String profileId, Long parentTweetId);
}
