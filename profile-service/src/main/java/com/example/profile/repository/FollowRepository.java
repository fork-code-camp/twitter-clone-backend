package com.example.profile.repository;

import com.example.profile.entity.Follow;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends MongoRepository<Follow, String> {

    Optional<Follow> deleteByFollowerProfile_IdAndFolloweeProfile_Id(String followerId, String followeeId);

    boolean existsByFollowerProfile_IdAndFolloweeProfile_Id(String followerId, String followeeId);

    List<Follow> findAllByFollowerProfile_Id(String followerId);

    List<Follow> findAllByFolloweeProfile_Id(String followeeId);

    Integer countAllByFollowerProfile_Id(String followerId);

    Integer countAllByFolloweeProfile_Id(String followeeId);
}
