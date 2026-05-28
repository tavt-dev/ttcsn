package com.friendify.app.social.service;

import java.util.List;

import com.friendify.app.shared.dto.PageResponse;
import com.friendify.app.shared.exception.AppException;
import com.friendify.app.shared.exception.ErrorCode;
import com.friendify.app.social.dto.response.UserBlockResponse;
import com.friendify.app.social.entity.UserBlock;
import com.friendify.app.social.mapper.UserBlockMapper;
import com.friendify.app.social.port.SocialGraphQueryPort;
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

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserBlockService implements SocialGraphQueryPort {
    UserBlockRepository userBlockRepository;
    FollowRepository followRepository;
    FriendshipRepository friendshipRepository;
    FriendshipService friendshipService;
    FollowService followService;
    UserBlockMapper userBlockMapper;

    @Transactional
    public UserBlockResponse blockUser(String blockerId, String blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new AppException(ErrorCode.CANNOT_BLOCK_SELF);
        }
        if (userBlockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
            throw new AppException(ErrorCode.USER_ALREADY_BLOCKED);
        }

        followRepository.findByFollowerIdAndFollowingId(blockerId, blockedId).ifPresent(followRepository::delete);
        followRepository.findByFollowerIdAndFollowingId(blockedId, blockerId).ifPresent(followRepository::delete);
        friendshipRepository.findByUserIdAndFriendId(blockerId, blockedId).ifPresent(friendshipRepository::delete);
        friendshipRepository.findByUserIdAndFriendId(blockedId, blockerId).ifPresent(friendshipRepository::delete);

        UserBlock userBlock = UserBlock.builder()
                .blockedId(blockedId)
                .blockerId(blockerId)
                .build();
        userBlock = userBlockRepository.save(userBlock);
        log.info("User {} blocked user {}", blockerId, blockedId);
        return userBlockMapper.toUserBlockResponse(userBlock);
    }

    @Transactional
    public void unblockUser(String blockerId, String blockedId) {
        UserBlock userBlock = userBlockRepository
                .findByBlockerIdAndBlockedId(blockerId, blockedId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_BLOCKED));
        userBlockRepository.delete(userBlock);
        log.info("User {} unblocked user {}", blockerId, blockedId);
    }

    public PageResponse<UserBlockResponse> getBlockedUsers(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        var pageData = userBlockRepository.findBlockedUsersByUserId(userId, pageable);
        var responses = pageData.getContent().stream()
                .map(userBlockMapper::toUserBlockResponse)
                .toList();
        return PageResponse.<UserBlockResponse>builder()
                .content(responses)
                .page(page)
                .size(size)
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .build();
    }

    @Override
    public List<String> getBlockedUserIds(String userId) {
        return userBlockRepository.findBlockedUserIds(userId);
    }

    public boolean checkBlocked(String blockerId, String blockedId) {
        return userBlockRepository.isBlocked(blockerId, blockedId);
    }

    @Override
    public List<String> getFriendIds(String currentUserId) {
        return friendshipService.getFriendIds(currentUserId);
    }

    @Override
    public List<String> getFollowingIds(String currentUserId) {
        return followService.getFollowingIds(currentUserId);
    }

    @Override
    public boolean isBlockedBetween(String userId1, String userId2) {
        return userBlockRepository.isBlocked(userId1, userId2);
    }
}
