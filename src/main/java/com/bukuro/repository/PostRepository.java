package com.bukuro.repository;

import com.bukuro.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Post> findByUserIdAndIsPublicTrueOrderByCreatedAtDesc(Long userId);

    List<Post> findByUserIdInAndIsPublicTrueOrderByCreatedAtDesc(List<Long> userIds, Pageable pageable);

    List<Post> findByIsPublicTrueOrderByGoodCountDescCreatedAtDesc(Pageable pageable);

    @Modifying
    @Query("UPDATE Post p SET p.goodCount = p.goodCount + 1 WHERE p.id = :postId")
    void incrementGoodCount(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE Post p SET p.goodCount = p.goodCount - 1 WHERE p.id = :postId AND p.goodCount > 0")
    void decrementGoodCount(@Param("postId") Long postId);
}
