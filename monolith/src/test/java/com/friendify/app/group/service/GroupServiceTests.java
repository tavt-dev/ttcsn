package com.friendify.app.group.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.friendify.app.file.dto.UploadResponse;
import com.friendify.app.file.port.FileUploadPort;
import com.friendify.app.group.dto.response.GroupResponse;
import com.friendify.app.group.entity.Group;
import com.friendify.app.group.entity.GroupMember;
import com.friendify.app.group.enums.GroupPrivacy;
import com.friendify.app.group.enums.MemberRole;
import com.friendify.app.group.mapper.GroupMapper;
import com.friendify.app.group.mapper.GroupMemberMapper;
import com.friendify.app.group.mapper.JoinRequestMapper;
import com.friendify.app.group.port.GroupAccessPort;
import com.friendify.app.group.repository.GroupMemberRepository;
import com.friendify.app.group.repository.GroupRepository;
import com.friendify.app.group.repository.JoinRequestRepository;
import com.friendify.app.profile.dto.response.ProfileResponse;
import com.friendify.app.profile.port.ProfileQueryPort;
import com.friendify.app.shared.media.ImageType;
import com.friendify.app.shared.security.CurrentUserProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class GroupServiceTests {

    @Mock
    GroupRepository groupRepository;

    @Mock
    GroupMemberRepository groupMemberRepository;

    @Mock
    JoinRequestRepository joinRequestRepository;

    @Mock
    GroupMapper groupMapper;

    @Mock
    GroupMemberMapper groupMemberMapper;

    @Mock
    JoinRequestMapper joinRequestMapper;

    @Mock
    ProfileQueryPort profileQueryPort;

    @Mock
    FileUploadPort fileUploadPort;

    @Mock
    DateTimeFormatter dateTimeFormatter;

    @Mock
    CurrentUserProvider currentUserProvider;

    @InjectMocks
    GroupService groupService;

    @Test
    void implementsGroupAccessPort() {
        assertThat(groupService).isInstanceOf(GroupAccessPort.class);
    }

    @Test
    void uploadGroupAvatarUsesFileUploadPort() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "avatar".getBytes());
        Group group = Group.builder()
                .id("group-1")
                .ownerId("owner-1")
                .name("Friends")
                .privacy(GroupPrivacy.PUBLIC)
                .allowPosting(true)
                .build();
        GroupResponse mappedResponse = GroupResponse.builder()
                .id("group-1")
                .ownerId("owner-1")
                .name("Friends")
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn("owner-1");
        when(groupRepository.findById("group-1")).thenReturn(Optional.of(group));
        when(fileUploadPort.uploadImage(eq(file), eq(ImageType.GROUP_AVATAR), eq("group-1"), eq(null)))
                .thenReturn(new UploadResponse("public-id", 1L, 100, 100, "https://cdn.example/group-avatar.png", null));
        when(groupRepository.save(group)).thenReturn(group);
        when(groupMapper.toGroupResponse(group)).thenReturn(mappedResponse);
        when(profileQueryPort.getProfileByUserId("owner-1")).thenReturn(ProfileResponse.builder()
                .userId("owner-1")
                .username("owner")
                .build());

        GroupResponse result = groupService.uploadGroupAvatar("group-1", file);

        assertThat(result).isSameAs(mappedResponse);
        assertThat(group.getAvatarUrl()).isEqualTo("https://cdn.example/group-avatar.png");
        verify(fileUploadPort).uploadImage(eq(file), eq(ImageType.GROUP_AVATAR), eq("group-1"), eq(null));
    }

    @Test
    void canPostAllowsModeratorWhenOnlyAdminCanPost() {
        Group group = Group.builder()
                .id("group-1")
                .ownerId("owner-1")
                .privacy(GroupPrivacy.PRIVATE)
                .allowPosting(true)
                .onlyAdminCanPost(true)
                .build();

        when(groupRepository.findById("group-1")).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupIdAndUserId("group-1", "moderator-1"))
                .thenReturn(Optional.of(GroupMember.builder()
                        .groupId("group-1")
                        .userId("moderator-1")
                        .role(MemberRole.MODERATOR)
                        .build()));

        assertThat(groupService.canPost("group-1", "moderator-1")).isTrue();
    }
}
