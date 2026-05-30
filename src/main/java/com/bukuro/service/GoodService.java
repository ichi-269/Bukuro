package com.bukuro.service;

import com.bukuro.entity.Good;
import com.bukuro.exception.DuplicateRecordException;
import com.bukuro.exception.ResourceNotFoundException;
import com.bukuro.repository.GoodRepository;
import com.bukuro.repository.PostRepository;
import com.bukuro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoodService {

    private final GoodRepository goodRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public void addGood(Long userId, Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("記事が見つかりません: " + postId);
        }
        if (goodRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new DuplicateRecordException("すでにグッド済みです");
        }
        Good good = Good.builder()
                .user(userRepository.getReferenceById(userId))
                .post(postRepository.getReferenceById(postId))
                .build();
        goodRepository.save(good);
        postRepository.incrementGoodCount(postId);
    }

    @Transactional
    public void removeGood(Long userId, Long postId) {
        if (!goodRepository.existsByUserIdAndPostId(userId, postId)) {
            return;
        }
        goodRepository.deleteByUserIdAndPostId(userId, postId);
        postRepository.decrementGoodCount(postId);
    }

    @Transactional(readOnly = true)
    public boolean hasGooded(Long userId, Long postId) {
        return goodRepository.existsByUserIdAndPostId(userId, postId);
    }
}
