package com.example.tweets.entity;

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
        indexes = {
                @Index(columnList = "reply_to_id", name = "reply_to_id"),
                @Index(columnList = "quote_to_id", name = "quote_to_id")
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

    @OneToMany(
            targetEntity = Like.class,
            mappedBy = "parentTweet",
            cascade = CascadeType.ALL
    )
    private Set<Like> likes = new HashSet<>();

    @OneToMany(
            targetEntity = Retweet.class,
            mappedBy = "parentTweet",
            cascade = CascadeType.ALL
    )
    private Set<Retweet> retweets = new HashSet<>();

    @OneToMany(
            targetEntity = Tweet.class,
            mappedBy = "replyTo",
            cascade = CascadeType.ALL
    )
    private Set<Tweet> replies = new HashSet<>();

    @ManyToOne(targetEntity = Tweet.class)
    @Nullable
    private Tweet replyTo;

    @ManyToOne(targetEntity = Tweet.class)
    @Nullable
    private Tweet quoteTo;
}
