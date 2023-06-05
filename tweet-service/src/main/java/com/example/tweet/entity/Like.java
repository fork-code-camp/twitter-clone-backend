package com.example.tweet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"parent_tweet_id", "profileId"}),
        indexes = {
                @Index(columnList = "parent_tweet_id")
        }
)
public class Like implements BaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String profileId;

    @ManyToOne(targetEntity = Tweet.class)
    private Tweet parentTweet;
}
