package com.example.tweets.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "retweets", uniqueConstraints = @UniqueConstraint(columnNames = {"tweet_id", "profileId"}))
public class Retweet implements BaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(targetEntity = Tweet.class)
    private Tweet tweet;
    private String profileId;
    private LocalDateTime retweetTime;
}
