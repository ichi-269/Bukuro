package com.bukuro.repository;

import com.bukuro.entity.Follow;
import com.bukuro.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    Optional<Follow> findByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    long countByFolloweeId(Long followeeId);

    long countByFollowerId(Long followerId);

    @Query("SELECT f.followee.id FROM Follow f WHERE f.follower.id = :followerId")
    List<Long> findFolloweeIdsByFollowerId(@Param("followerId") Long followerId);

    @Query("SELECT f.follower FROM Follow f WHERE f.followee.id = :userId")
    List<User> findFollowersByFolloweeId(@Param("userId") Long userId);

    @Query("SELECT f.followee FROM Follow f WHERE f.follower.id = :userId")
    List<User> findFolloweesByFollowerId(@Param("userId") Long userId);
}
