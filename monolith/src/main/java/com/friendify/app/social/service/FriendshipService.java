package com.friendify.app.social.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.friendify.app.profile.dto.request.SearchUserRequest;
import com.friendify.app.profile.dto.response.ProfileResponse;
import com.friendify.app.profile.port.ProfileQueryPort;
import com.friendify.app.shared.dto.PageResponse;
import com.friendify.app.shared.exception.AppException;
import com.friendify.app.shared.exception.ErrorCode;
import com.friendify.app.social.dto.response.FriendshipResponse;
import com.friendify.app.social.dto.response.SocialCountsResponse;
import com.friendify.app.social.entity.Friendship;
import com.friendify.app.social.enums.FriendshipStatus;
import com.friendify.app.social.mapper.FriendshipMapper;
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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequiredArgsConstructor
public class FriendshipService {
    FriendshipRepository friendshipRepository;
    UserBlockRepository userBlockRepository;
    FriendshipMapper friendshipMapper;
    ProfileQueryPort profileQueryPort;
    FollowRepository followRepository;

    @Transactional
    public FriendshipResponse sendFriendRequest(String userId, String friendId) {
        if (userId.equals(friendId)) {
            throw new AppException(ErrorCode.CANNOT_FRIEND_SELF);
        }
        if (friendshipRepository.existsByUserIdAndFriendId(userId, friendId)
                || friendshipRepository.existsByUserIdAndFriendId(friendId, userId)) {
            throw new AppException(ErrorCode.FRIENDSHIP_ALREADY_EXISTS);
        }
        if (userBlockRepository.isBlocked(userId, friendId)) {
            throw new AppException(ErrorCode.USER_ALREADY_BLOCKED);
        }

        Friendship friendship = Friendship.builder()
                .userId(userId)
                .friendId(friendId)
                .status(FriendshipStatus.PENDING)
                .build();

        friendship = friendshipRepository.save(friendship);
        log.info("User {} sent friend request to user {}", userId, friendId);
        return friendshipMapper.toFriendshipResponse(friendship);
    }

    @Transactional
    public FriendshipResponse acceptFriendRequest(String userId, String friendId) {
        Friendship friendship = friendshipRepository
                .findByUserIdAndFriendIdAndStatus(friendId, userId, FriendshipStatus.PENDING)
                .orElseThrow(() -> new AppException(ErrorCode.FRIEND_REQUEST_NOT_PENDING));

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendship = friendshipRepository.save(friendship);
        log.info("User {} accepted friend request from user {}", userId, friendId);
        return friendshipMapper.toFriendshipResponse(friendship);
    }

    @Transactional
    public void rejectFriendRequest(String userId, String friendId) {
        Friendship friendship = friendshipRepository
                .findByUserIdAndFriendIdAndStatus(friendId, userId, FriendshipStatus.PENDING)
                .orElseThrow(() -> new AppException(ErrorCode.FRIEND_REQUEST_NOT_PENDING));
        friendshipRepository.delete(friendship);
        log.info("User {} rejected friend request from user {}", userId, friendId);
    }

    @Transactional
    public void removeFriend(String userId, String friendId) {
        if (!friendshipRepository.areFriends(userId, friendId)) {
            throw new AppException(ErrorCode.FRIENDSHIP_NOT_FOUND);
        }
        friendshipRepository.deleteFriendship(userId, friendId);
        log.info("User {} removed friend {}", userId, friendId);
    }

    public PageResponse<FriendshipResponse> getFriends(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        var pageData = friendshipRepository.findFriendshipsByUserIdAndStatus(
                userId, FriendshipStatus.ACCEPTED, pageable);
        var friendshipResponses = pageData.map(friendshipMapper::toFriendshipResponse);
        return PageResponse.<FriendshipResponse>builder()
                .page(page)
                .totalPages(pageData.getTotalPages())
                .size(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .content(friendshipResponses.getContent())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .build();
    }

    public PageResponse<FriendshipResponse> getSentFriendRequests(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        var pageData = friendshipRepository.findSentFriendRequests(userId, FriendshipStatus.PENDING, pageable);
        var responses = pageData.map(friendshipMapper::toFriendshipResponse);
        return PageResponse.<FriendshipResponse>builder()
                .page(page)
                .totalPages(pageData.getTotalPages())
                .size(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .content(responses.getContent())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .build();
    }

    public PageResponse<FriendshipResponse> getReceivedFriendRequests(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        var pageData = friendshipRepository.findReceivedFriendRequests(userId, FriendshipStatus.PENDING, pageable);
        var responses = pageData.map(friendshipMapper::toFriendshipResponse);
        return PageResponse.<FriendshipResponse>builder()
                .page(page)
                .totalPages(pageData.getTotalPages())
                .size(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .content(responses.getContent())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .build();
    }

    public List<ProfileResponse> searchFriends(String userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_KEYWORD);
        }

        try {
            var blockedUserIds = new HashSet<>(userBlockRepository.findBlockedUserIds(userId));
            blockedUserIds.add(userId);
            var profiles = profileQueryPort.search(SearchUserRequest.builder()
                    .keyword(keyword.trim())
                    .build());
            return profiles.stream()
                    .filter(profile -> !blockedUserIds.contains(profile.getUserId()))
                    .toList();
        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("Error while searching friends", exception);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    public String getFriendshipStatus(String userId, String friendId) {
        if (userId.equals(friendId)) {
            return "SELF";
        }

        Optional<Friendship> friendship1 = friendshipRepository.findByUserIdAndFriendId(userId, friendId);
        Optional<Friendship> friendship2 = friendshipRepository.findByUserIdAndFriendId(friendId, userId);

        if (friendship1.isPresent()) {
            return switch (friendship1.get().getStatus()) {
                case ACCEPTED -> "ACCEPTED";
                case PENDING -> "SENT";
                case BLOCKED -> "REJECTED";
            };
        }
        if (friendship2.isPresent()) {
            return switch (friendship2.get().getStatus()) {
                case ACCEPTED -> "ACCEPTED";
                case PENDING -> "RECEIVED";
                case BLOCKED -> "REJECTED";
            };
        }
        return "NONE";
    }

    public List<String> getFriendIds(String userId) {
        return friendshipRepository.findAllFriends(userId).stream()
                .map(friendship -> friendship.getUserId().equals(userId)
                        ? friendship.getFriendId()
                        : friendship.getUserId())
                .toList();
    }

    public List<ProfileResponse> getMutualFriends(String userId1, String userId2) {
        if (userId1.equals(userId2)) {
            return List.of();
        }

        try {
            List<String> mutualFriendIds = friendshipRepository.findMutualFriendIds(userId1, userId2);
            if (mutualFriendIds.isEmpty()) {
                return List.of();
            }
            var blockedUserIds = new HashSet<>(userBlockRepository.findBlockedUserIds(userId1));
            blockedUserIds.add(userId1);

            List<ProfileResponse> mutualFriends = new ArrayList<>();
            for (String friendId : mutualFriendIds) {
                if (!blockedUserIds.contains(friendId)) {
                    try {
                        mutualFriends.add(profileQueryPort.getProfileByUserId(friendId));
                    } catch (Exception exception) {
                        log.warn("Failed to get profile for user {}: {}", friendId, exception.getMessage());
                    }
                }
            }
            return mutualFriends;
        } catch (Exception exception) {
            log.error("Error while getting mutual friends", exception);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    public List<ProfileResponse> getSuggestedFriends(String userId, int limit) {
        try {
            List<String> friendIds = getFriendIds(userId);
            if (friendIds.isEmpty()) {
                return List.of();
            }
            var blockedUserIds = new HashSet<>(userBlockRepository.findBlockedUserIds(userId));
            blockedUserIds.add(userId);
            Map<String, Integer> mutualCountMap = new HashMap<>();

            for (String friendId : friendIds) {
                for (Friendship friendship : friendshipRepository.findAllFriends(friendId)) {
                    String suggestedUserId = friendship.getUserId().equals(friendId)
                            ? friendship.getFriendId()
                            : friendship.getUserId();
                    if (suggestedUserId.equals(userId)
                            || friendIds.contains(suggestedUserId)
                            || blockedUserIds.contains(suggestedUserId)) {
                        continue;
                    }
                    mutualCountMap.put(suggestedUserId, mutualCountMap.getOrDefault(suggestedUserId, 0) + 1);
                }
            }

            List<String> suggestedUserIds = mutualCountMap.entrySet().stream()
                    .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                    .limit(limit)
                    .map(Map.Entry::getKey)
                    .toList();
            if (suggestedUserIds.isEmpty()) {
                return List.of();
            }

            List<ProfileResponse> suggestedFriends = new ArrayList<>();
            for (String suggestedUserId : suggestedUserIds) {
                try {
                    suggestedFriends.add(profileQueryPort.getProfileByUserId(suggestedUserId));
                } catch (Exception exception) {
                    log.warn("Failed to get profile for user {}: {}", suggestedUserId, exception.getMessage());
                }
            }
            return suggestedFriends;
        } catch (Exception exception) {
            log.error("Error while getting suggested friends", exception);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    public long getPendingFriendRequestsCount(String userId) {
        return friendshipRepository.countReceivedFriendRequests(userId, FriendshipStatus.PENDING);
    }

    @Transactional
    public void cancelFriendRequest(String userId, String friendId) {
        Friendship friendship = friendshipRepository
                .findByUserIdAndFriendIdAndStatus(userId, friendId, FriendshipStatus.PENDING)
                .orElseThrow(() -> new AppException(ErrorCode.FRIEND_REQUEST_NOT_PENDING));
        friendshipRepository.delete(friendship);
        log.info("User {} cancelled friend request to user {}", userId, friendId);
    }

    public long getFriendCount(String userId) {
        return friendshipRepository.countFriends(userId);
    }

    public long getSentFriendRequestsCount(String userId) {
        return friendshipRepository.countSentFriendRequests(userId, FriendshipStatus.PENDING);
    }

    public Map<String, String> batchCheckFriendshipStatus(String userId, List<String> friendIds) {
        Map<String, String> statusMap = new HashMap<>();
        for (String friendId : friendIds) {
            statusMap.put(friendId, getFriendshipStatus(userId, friendId));
        }
        return statusMap;
    }

    public SocialCountsResponse getAllSocialCounts(String userId) {
        return SocialCountsResponse.builder()
                .friendsCount(getFriendCount(userId))
                .followersCount(followRepository.countByFollowingId(userId))
                .followingCount(followRepository.countByFollowerId(userId))
                .pendingFriendRequestsCount(getPendingFriendRequestsCount(userId))
                .sentFriendRequestsCount(getSentFriendRequestsCount(userId))
                .blockedUsersCount(userBlockRepository.countByBlockerId(userId))
                .build();
    }
}
