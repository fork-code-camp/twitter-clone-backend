package com.example.tweet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "views",
        uniqueConstraints = @UniqueConstraint(columnNames = {"parent_tweet_id", "profileId"}),
        indexes = {
                @Index(columnList = "parent_tweet_id")
        }
)
public class View implements BaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String profileId;

    @ManyToOne(targetEntity = Tweet.class)
    private Tweet parentTweet;
}
