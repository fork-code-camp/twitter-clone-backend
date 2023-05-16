package com.example.tweets.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tweets", uniqueConstraints = @UniqueConstraint(columnNames = {"original_tweet_id", "profileId"}))
public class Tweet implements BaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(
            targetEntity = Like.class,
            mappedBy = "tweet",
            cascade = CascadeType.ALL
    )
    private List<Like> likes = new ArrayList<>();

    @OneToOne(targetEntity = Tweet.class)
    @Nullable
    private Tweet originalTweet;
    private String text;
    private String profileId;
    private LocalDateTime creationDate;
}
