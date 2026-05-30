package com.bukuro.service;

import com.bukuro.entity.Follow;
import com.bukuro.entity.User;
import com.bukuro.repository.FollowRepository;
import com.bukuro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public void follow(Long followerId, Long followeeId) {
        if (followerId.equals(followeeId)) {
            throw new IllegalArgumentException("自分自身をフォローすることはできません");
        }
        if (followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            return;
        }
        Follow follow = Follow.builder()
                .follower(userRepository.getReferenceById(followerId))
                .followee(userRepository.getReferenceById(followeeId))
                .build();
        followRepository.save(follow);
    }

    @Transactional
    public void unfollow(Long followerId, Long followeeId) {
        followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)
                .ifPresent(followRepository::delete);
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long followeeId) {
        return followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId);
    }

    @Transactional(readOnly = true)
    public long getFollowerCount(Long userId) {
        return followRepository.countByFolloweeId(userId);
    }

    @Transactional(readOnly = true)
    public long getFollowingCount(Long userId) {
        return followRepository.countByFollowerId(userId);
    }

    @Transactional(readOnly = true)
    public List<User> getFollowers(Long userId) {
        return followRepository.findFollowersByFolloweeId(userId);
    }

    @Transactional(readOnly = true)
    public List<User> getFollowees(Long userId) {
        return followRepository.findFolloweesByFollowerId(userId);
    }
}
