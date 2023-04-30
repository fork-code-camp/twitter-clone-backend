package com.example.profile.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "follows")
public class Follow implements BaseEntity<String> {

    @Id
    private String id;

    @DBRef @Indexed private Profile followerProfile;
    @DBRef @Indexed private Profile followeeProfile;

    private LocalDateTime followDateTime;
}
