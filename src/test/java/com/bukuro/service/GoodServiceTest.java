package com.bukuro.service;

import com.bukuro.entity.Good;
import com.bukuro.entity.Post;
import com.bukuro.entity.User;
import com.bukuro.exception.DuplicateRecordException;
import com.bukuro.exception.ResourceNotFoundException;
import com.bukuro.repository.GoodRepository;
import com.bukuro.repository.PostRepository;
import com.bukuro.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoodServiceTest {

    @Mock
    private GoodRepository goodRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GoodService goodService;

    @Test
    @DisplayName("addGood で正常にグッドが保存され good_count がインクリメントされる")
    void addGood_newGood_savesAndIncrements() {
        when(postRepository.existsById(1L)).thenReturn(true);
        when(goodRepository.existsByUserIdAndPostId(10L, 1L)).thenReturn(false);
        when(userRepository.getReferenceById(10L))
                .thenReturn(User.builder().id(10L).build());
        when(postRepository.getReferenceById(1L))
                .thenReturn(Post.builder().id(1L).build());

        goodService.addGood(10L, 1L);

        verify(goodRepository).save(any(Good.class));
        verify(postRepository).incrementGoodCount(1L);
    }

    @Test
    @DisplayName("addGood で既にグッド済みの場合 DuplicateRecordException が発生する")
    void addGood_alreadyGooded_throwsDuplicateRecordException() {
        when(postRepository.existsById(1L)).thenReturn(true);
        when(goodRepository.existsByUserIdAndPostId(10L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> goodService.addGood(10L, 1L))
                .isInstanceOf(DuplicateRecordException.class);

        verify(goodRepository, never()).save(any());
        verify(postRepository, never()).incrementGoodCount(any());
    }

    @Test
    @DisplayName("addGood で存在しない postId の場合 ResourceNotFoundException が発生する")
    void addGood_postNotFound_throwsResourceNotFoundException() {
        when(postRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> goodService.addGood(10L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(goodRepository, never()).save(any());
    }

    @Test
    @DisplayName("removeGood でグッド関係が削除され good_count がデクリメントされる")
    void removeGood_existingGood_deletesAndDecrements() {
        when(goodRepository.existsByUserIdAndPostId(10L, 1L)).thenReturn(true);

        goodService.removeGood(10L, 1L);

        verify(goodRepository).deleteByUserIdAndPostId(10L, 1L);
        verify(postRepository).decrementGoodCount(1L);
    }

    @Test
    @DisplayName("removeGood でグッドしていない場合は何もしない（冪等）")
    void removeGood_notGooded_doesNothing() {
        when(goodRepository.existsByUserIdAndPostId(10L, 1L)).thenReturn(false);

        goodService.removeGood(10L, 1L);

        verify(goodRepository, never()).deleteByUserIdAndPostId(any(), any());
        verify(postRepository, never()).decrementGoodCount(any());
    }

    @Test
    @DisplayName("hasGooded でグッド済みの場合 true を返す")
    void hasGooded_gooded_returnsTrue() {
        when(goodRepository.existsByUserIdAndPostId(10L, 1L)).thenReturn(true);

        assertThat(goodService.hasGooded(10L, 1L)).isTrue();
    }

    @Test
    @DisplayName("hasGooded でグッドしていない場合 false を返す")
    void hasGooded_notGooded_returnsFalse() {
        when(goodRepository.existsByUserIdAndPostId(10L, 1L)).thenReturn(false);

        assertThat(goodService.hasGooded(10L, 1L)).isFalse();
    }
}
