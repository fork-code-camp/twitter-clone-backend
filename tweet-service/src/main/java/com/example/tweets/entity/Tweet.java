package com.example.tweets.entity;

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
@Table(name = "tweets")
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
    @OneToMany(
            targetEntity = Retweet.class,
            mappedBy = "tweet",
            cascade = CascadeType.ALL
    )
    private List<Retweet> retweets = new ArrayList<>();

    private String text;
    private String profileId;
    private LocalDateTime creationDate;
}
