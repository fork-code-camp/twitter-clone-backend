package com.example.tweet.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "tweets",
        uniqueConstraints = @UniqueConstraint(columnNames = {"retweet_to_id", "profileId"}),
        indexes = {
                @Index(columnList = "reply_to_id", name = "reply_to_id"),
                @Index(columnList = "quote_to_id", name = "quote_to_id"),
                @Index(columnList = "retweet_to_id", name = "retweet_to_id")
        }
)
public class Tweet implements BaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    private String text;
    private String profileId;
    private LocalDateTime creationDate;
    @ElementCollection
    private Set<String> mediaUrls = new HashSet<>();

    @OneToMany(
            targetEntity = Like.class,
            mappedBy = "parentTweet",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Like> likes = new HashSet<>();

    @OneToMany(
            targetEntity = Tweet.class,
            mappedBy = "retweetTo",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Tweet> retweets = new HashSet<>();

    @OneToMany(
            targetEntity = Tweet.class,
            mappedBy = "replyTo",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Tweet> replies = new HashSet<>();

    @OneToMany(
            targetEntity = View.class,
            mappedBy = "parentTweet",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<View> views = new HashSet<>();

    @ManyToOne(targetEntity = Tweet.class)
    @Nullable
    private Tweet retweetTo;

    @ManyToOne(targetEntity = Tweet.class)
    @Nullable
    private Tweet replyTo;

    @ManyToOne(targetEntity = Tweet.class)
    @Nullable
    private Tweet quoteTo;
}
