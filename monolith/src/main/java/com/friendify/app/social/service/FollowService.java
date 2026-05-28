package com.friendify.app.social.service;

import java.util.List;

import com.friendify.app.shared.dto.PageResponse;
import com.friendify.app.shared.exception.AppException;
import com.friendify.app.shared.exception.ErrorCode;
import com.friendify.app.social.dto.response.FollowResponse;
import com.friendify.app.social.dto.response.UserSocialInfoResponse;
import com.friendify.app.social.entity.Follow;
import com.friendify.app.social.mapper.FollowMapper;
import com.friendify.app.social.repository.FollowRepository;
import com.friendify.app.social.repository.FriendshipRepository;
import com.friendify.app.social.repository.UserBlockRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class FollowService {
    FollowRepository followRepository;
    UserBlockRepository userBlockRepository;
    FriendshipRepository friendshipRepository;
    FollowMapper followMapper;

    @Transactional
    public FollowResponse followUser(String followerId, String followingId) {
        if (followerId.equals(followingId)) {
            throw new AppException(ErrorCode.CANNOT_FOLLOW_SELF);
        }
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new AppException(ErrorCode.FOLLOW_ALREADY_EXISTS);
        }
        if (userBlockRepository.isBlocked(followerId, followingId)) {
            throw new AppException(ErrorCode.USER_ALREADY_BLOCKED);
        }

        Follow follow = Follow.builder()
                .followerId(followerId)
                .followingId(followingId)
                .build();
        follow = followRepository.save(follow);
        log.info("User {} followed user {}", followerId, followingId);
        return followMapper.toFollowResponse(follow);
    }

    @Transactional
    public void unfollowUser(String followerId, String followingId) {
        Follow follow = followRepository
                .findByFollowerIdAndFollowingId(followerId, followingId)
                .orElseThrow(() -> new AppException(ErrorCode.FOLLOW_NOT_FOUND));
        followRepository.delete(follow);
        log.info("User {} unfollowed user {}", followerId, followingId);
    }

    public PageResponse<FollowResponse> getFollowingUser(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        var pageData = followRepository.findFollowingByUserId(userId, pageable);
        var responses = pageData.getContent().stream()
                .map(followMapper::toFollowResponse)
                .toList();
        return PageResponse.<FollowResponse>builder()
                .content(responses)
                .page(page)
                .size(size)
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .build();
    }

    public PageResponse<FollowResponse> getFollowerUser(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        var pageData = followRepository.findFollowersByUserId(userId, pageable);
        var responses = pageData.getContent().stream()
                .map(followMapper::toFollowResponse)
                .toList();
        return PageResponse.<FollowResponse>builder()
                .content(responses)
                .page(page)
                .size(size)
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .build();
    }

    public UserSocialInfoResponse getUserSocialInfo(String currentUserId, String userId) {
        long followingCount = followRepository.countByFollowerId(userId);
        long followerCount = followRepository.countByFollowingId(userId);
        long friendCount = friendshipRepository.findAllFriends(userId).size();
        boolean isFollowing = false;
        boolean isFriend = false;
        boolean isBlocked = false;

        if (currentUserId != null && !currentUserId.equals(userId)) {
            isFollowing = followRepository.existsByFollowerIdAndFollowingId(currentUserId, userId);
            isFriend = friendshipRepository.areFriends(currentUserId, userId);
            isBlocked = userBlockRepository.isBlocked(currentUserId, userId);
        }

        return UserSocialInfoResponse.builder()
                .userId(userId)
                .followingCount(followingCount)
                .followersCount(followerCount)
                .friendsCount(friendCount)
                .isFollowing(isFollowing)
                .isFriend(isFriend)
                .isBlocked(isBlocked)
                .build();
    }

    public List<String> getFollowingIds(String userId) {
        return followRepository.findFollowingIdsByUserId(userId);
    }
}
