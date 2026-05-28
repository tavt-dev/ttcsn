package com.friendify.app.social.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import com.friendify.app.profile.dto.request.SearchUserRequest;
import com.friendify.app.profile.dto.response.ProfileResponse;
import com.friendify.app.profile.port.ProfileQueryPort;
import com.friendify.app.shared.exception.AppException;
import com.friendify.app.shared.exception.ErrorCode;
import com.friendify.app.social.mapper.FriendshipMapper;
import com.friendify.app.social.repository.FollowRepository;
import com.friendify.app.social.repository.FriendshipRepository;
import com.friendify.app.social.repository.UserBlockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceTests {

    @Mock
    FriendshipRepository friendshipRepository;

    @Mock
    UserBlockRepository userBlockRepository;

    @Mock
    FriendshipMapper friendshipMapper;

    @Mock
    ProfileQueryPort profileQueryPort;

    @Mock
    FollowRepository followRepository;

    @InjectMocks
    FriendshipService friendshipService;

    @Test
    void searchFriendsUsesProfileQueryPortAndFiltersBlockedUsers() {
        ProfileResponse blockedProfile = ProfileResponse.builder()
                .userId("blocked-user")
                .username("blocked")
                .build();
        ProfileResponse visibleProfile = ProfileResponse.builder()
                .userId("visible-user")
                .username("visible")
                .build();

        when(userBlockRepository.findBlockedUserIds("current-user")).thenReturn(List.of("blocked-user"));
        when(profileQueryPort.search(any(SearchUserRequest.class))).thenReturn(List.of(blockedProfile, visibleProfile));

        List<ProfileResponse> result = friendshipService.searchFriends("current-user", " alice ");

        assertThat(result).containsExactly(visibleProfile);
        ArgumentCaptor<SearchUserRequest> requestCaptor = ArgumentCaptor.forClass(SearchUserRequest.class);
        verify(profileQueryPort).search(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getKeyword()).isEqualTo("alice");
    }

    @Test
    void sendFriendRequestRejectsSelfWithoutRepositoryWrite() {
        assertThatThrownBy(() -> friendshipService.sendFriendRequest("user-1", "user-1"))
                .isInstanceOf(AppException.class)
                .extracting(exception -> ((AppException) exception).getErrorCode())
                .isEqualTo(ErrorCode.CANNOT_FRIEND_SELF);

        verifyNoInteractions(friendshipRepository);
    }
}
