package com.bukuro.service;

import com.bukuro.entity.Follow;
import com.bukuro.entity.User;
import com.bukuro.repository.FollowRepository;
import com.bukuro.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FollowService followService;

    private User buildUser(Long id) {
        return User.builder().id(id).username("user" + id).email("user" + id + "@example.com").build();
    }

    @Test
    @DisplayName("follow で新規フォロー関係が保存される")
    void follow_newRelation_savesFollow() {
        when(followRepository.existsByFollowerIdAndFolloweeId(1L, 2L)).thenReturn(false);
        when(userRepository.getReferenceById(1L)).thenReturn(buildUser(1L));
        when(userRepository.getReferenceById(2L)).thenReturn(buildUser(2L));

        followService.follow(1L, 2L);

        verify(followRepository).save(any(Follow.class));
    }

    @Test
    @DisplayName("follow で既にフォロー済みの場合は保存をスキップする（冪等）")
    void follow_alreadyFollowing_skips() {
        when(followRepository.existsByFollowerIdAndFolloweeId(1L, 2L)).thenReturn(true);

        followService.follow(1L, 2L);

        verify(followRepository, never()).save(any());
    }

    @Test
    @DisplayName("follow で自分自身をフォローしようとすると IllegalArgumentException が発生する")
    void follow_selfFollow_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> followService.follow(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class);

        verify(followRepository, never()).save(any());
    }

    @Test
    @DisplayName("unfollow でフォロー関係が削除される")
    void unfollow_existingRelation_deletesFollow() {
        Follow follow = Follow.builder()
                .id(1L).follower(buildUser(1L)).followee(buildUser(2L)).build();
        when(followRepository.findByFollowerIdAndFolloweeId(1L, 2L)).thenReturn(Optional.of(follow));

        followService.unfollow(1L, 2L);

        verify(followRepository).delete(follow);
    }

    @Test
    @DisplayName("unfollow でフォロー関係が存在しない場合は何もしない（冪等）")
    void unfollow_noRelation_doesNothing() {
        when(followRepository.findByFollowerIdAndFolloweeId(1L, 2L)).thenReturn(Optional.empty());

        followService.unfollow(1L, 2L);

        verify(followRepository, never()).delete(any());
    }

    @Test
    @DisplayName("isFollowing でフォロー中の場合 true を返す")
    void isFollowing_following_returnsTrue() {
        when(followRepository.existsByFollowerIdAndFolloweeId(1L, 2L)).thenReturn(true);

        assertThat(followService.isFollowing(1L, 2L)).isTrue();
    }

    @Test
    @DisplayName("isFollowing でフォローしていない場合 false を返す")
    void isFollowing_notFollowing_returnsFalse() {
        when(followRepository.existsByFollowerIdAndFolloweeId(1L, 2L)).thenReturn(false);

        assertThat(followService.isFollowing(1L, 2L)).isFalse();
    }

    @Test
    @DisplayName("getFollowerCount でフォロワー数を返す")
    void getFollowerCount_returnsCount() {
        when(followRepository.countByFolloweeId(2L)).thenReturn(5L);

        assertThat(followService.getFollowerCount(2L)).isEqualTo(5L);
    }

    @Test
    @DisplayName("getFollowingCount でフォロー中数を返す")
    void getFollowingCount_returnsCount() {
        when(followRepository.countByFollowerId(1L)).thenReturn(3L);

        assertThat(followService.getFollowingCount(1L)).isEqualTo(3L);
    }

    @Test
    @DisplayName("getFollowers でフォロワーのユーザーリストを返す")
    void getFollowers_returnsFollowerList() {
        List<User> followers = List.of(buildUser(2L), buildUser(3L));
        when(followRepository.findFollowersByFolloweeId(1L)).thenReturn(followers);

        assertThat(followService.getFollowers(1L)).isEqualTo(followers);
    }

    @Test
    @DisplayName("getFollowees でフォロー中ユーザーのリストを返す")
    void getFollowees_returnsFolloweeList() {
        List<User> followees = List.of(buildUser(3L), buildUser(4L));
        when(followRepository.findFolloweesByFollowerId(1L)).thenReturn(followees);

        assertThat(followService.getFollowees(1L)).isEqualTo(followees);
    }
}
