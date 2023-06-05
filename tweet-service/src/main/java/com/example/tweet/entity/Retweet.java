package com.example.tweet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "retweets",
        uniqueConstraints = @UniqueConstraint(columnNames = {"parent_tweet_id", "profileId"}),
        indexes = {
                @Index(columnList = "parent_tweet_id")
        }
)
public class Retweet implements BaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String profileId;
    private LocalDateTime retweetTime;

    @ManyToOne(targetEntity = Tweet.class)
    private Tweet parentTweet;
}
