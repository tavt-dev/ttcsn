package com.friendify.app.group.service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import com.friendify.app.file.port.FileUploadPort;
import com.friendify.app.group.dto.request.CreateGroupRequest;
import com.friendify.app.group.dto.request.JoinGroupRequest;
import com.friendify.app.group.dto.request.ProcessJoinRequest;
import com.friendify.app.group.dto.request.UpdateGroupRequest;
import com.friendify.app.group.dto.request.UpdateMemberRoleRequest;
import com.friendify.app.group.dto.response.GroupMemberResponse;
import com.friendify.app.group.dto.response.GroupResponse;
import com.friendify.app.group.dto.response.JoinRequestResponse;
import com.friendify.app.group.dto.response.MemberRoleResponse;
import com.friendify.app.group.entity.Group;
import com.friendify.app.group.entity.GroupMember;
import com.friendify.app.group.entity.JoinRequest;
import com.friendify.app.group.enums.GroupPrivacy;
import com.friendify.app.group.enums.MemberRole;
import com.friendify.app.group.enums.RequestStatus;
import com.friendify.app.group.mapper.GroupMapper;
import com.friendify.app.group.mapper.GroupMemberMapper;
import com.friendify.app.group.mapper.JoinRequestMapper;
import com.friendify.app.group.port.GroupAccessPort;
import com.friendify.app.group.repository.GroupMemberRepository;
import com.friendify.app.group.repository.GroupRepository;
import com.friendify.app.group.repository.JoinRequestRepository;
import com.friendify.app.profile.dto.response.ProfileResponse;
import com.friendify.app.profile.port.ProfileQueryPort;
import com.friendify.app.shared.dto.PageResponse;
import com.friendify.app.shared.exception.AppException;
import com.friendify.app.shared.exception.ErrorCode;
import com.friendify.app.shared.media.ImageType;
import com.friendify.app.shared.security.CurrentUserProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupService implements GroupAccessPort {
    GroupRepository groupRepository;
    GroupMemberRepository groupMemberRepository;
    JoinRequestRepository joinRequestRepository;
    GroupMapper groupMapper;
    GroupMemberMapper groupMemberMapper;
    JoinRequestMapper joinRequestMapper;
    ProfileQueryPort profileQueryPort;
    FileUploadPort fileUploadPort;
    DateTimeFormatter dateTimeFormatter;
    CurrentUserProvider currentUserProvider;

    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request) {
        String userId = currentUserProvider.getCurrentUserId();

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new AppException(ErrorCode.GROUP_NAME_REQUIRED);
        }

        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .coverImageUrl(request.getCoverImageUrl())
                .avatarUrl(request.getAvatarUrl())
                .ownerId(userId)
                .privacy(request.getPrivacy() != null ? request.getPrivacy() : GroupPrivacy.PUBLIC)
                .requiresApproval(Boolean.TRUE.equals(request.getRequiresApproval()))
                .allowPosting(request.getAllowPosting() == null || Boolean.TRUE.equals(request.getAllowPosting()))
                .moderationRequired(Boolean.TRUE.equals(request.getModerationRequired()))
                .onlyAdminCanPost(Boolean.TRUE.equals(request.getOnlyAdminCanPost()))
                .createdDate(Instant.now())
                .modifiedDate(Instant.now())
                .build();

        group = groupRepository.save(group);

        groupMemberRepository.save(GroupMember.builder()
                .groupId(group.getId())
                .userId(userId)
                .role(MemberRole.ADMIN)
                .joinedDate(Instant.now())
                .build());

        return buildGroupResponse(group, userId);
    }

    @Transactional
    public GroupResponse updateGroup(String groupId, UpdateGroupRequest request) {
        String userId = currentUserProvider.getCurrentUserId();
        Group group = getGroupEntity(groupId);

        if (!group.getOwnerId().equals(userId)) {
            throw new AppException(ErrorCode.GROUP_NOT_OWNER);
        }

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            group.setName(request.getName());
        }
        if (request.getDescription() != null) {
            group.setDescription(request.getDescription());
        }
        if (request.getPrivacy() != null) {
            group.setPrivacy(request.getPrivacy());
        }
        if (request.getRequiresApproval() != null) {
            group.setRequiresApproval(request.getRequiresApproval());
        }
        if (request.getAllowPosting() != null) {
            group.setAllowPosting(request.getAllowPosting());
        }
        if (request.getModerationRequired() != null) {
            group.setModerationRequired(request.getModerationRequired());
        }
        if (request.getOnlyAdminCanPost() != null) {
            group.setOnlyAdminCanPost(request.getOnlyAdminCanPost());
        }

        group.setModifiedDate(Instant.now());
        return buildGroupResponse(groupRepository.save(group), userId);
    }

    @Transactional
    public GroupResponse uploadGroupAvatar(String groupId, MultipartFile file) {
        String userId = currentUserProvider.getCurrentUserId();
        Group group = getGroupEntity(groupId);
        checkOwnerOrAdmin(group, userId);
        validateUploadFile(file);

        try {
            var uploadResponse = fileUploadPort.uploadImage(file, ImageType.GROUP_AVATAR, groupId, null);
            group.setAvatarUrl(uploadResponse.getSecureUrl());
        } catch (IOException exception) {
            log.error("Failed to upload group avatar", exception);
            throw new AppException(ErrorCode.CLOUDINARY_UPLOAD_FAILED);
        }

        group.setModifiedDate(Instant.now());
        return buildGroupResponse(groupRepository.save(group), userId);
    }

    @Transactional
    public GroupResponse uploadGroupCover(String groupId, MultipartFile file) {
        String userId = currentUserProvider.getCurrentUserId();
        Group group = getGroupEntity(groupId);
        checkOwnerOrAdmin(group, userId);
        validateUploadFile(file);

        try {
            var uploadResponse = fileUploadPort.uploadImage(file, ImageType.GROUP_COVER, groupId, null);
            group.setCoverImageUrl(uploadResponse.getSecureUrl());
        } catch (IOException exception) {
            log.error("Failed to upload group cover", exception);
            throw new AppException(ErrorCode.CLOUDINARY_UPLOAD_FAILED);
        }

        group.setModifiedDate(Instant.now());
        return buildGroupResponse(groupRepository.save(group), userId);
    }

    @Transactional
    public void deleteGroup(String groupId) {
        String userId = currentUserProvider.getCurrentUserId();
        Group group = getGroupEntity(groupId);

        if (!group.getOwnerId().equals(userId)) {
            throw new AppException(ErrorCode.GROUP_NOT_OWNER);
        }

        groupMemberRepository.deleteAllByGroupId(groupId);
        joinRequestRepository.deleteAllByGroupId(groupId);
        groupRepository.delete(group);
    }

    public PageResponse<GroupResponse> getAllGroups(String privacy, int page, int size) {
        String userId = currentUserProvider.getCurrentUserId();
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdDate").descending());

        Page<Group> pageData;
        if (privacy != null && !privacy.trim().isEmpty()) {
            try {
                GroupPrivacy privacyFilter = GroupPrivacy.valueOf(privacy.toUpperCase());
                if (privacyFilter == GroupPrivacy.PRIVATE) {
                    throw new AppException(ErrorCode.UNAUTHORIZED);
                }
                pageData = groupRepository.findByPrivacy(privacyFilter, pageable);
            } catch (IllegalArgumentException exception) {
                throw new AppException(ErrorCode.INVALID_KEY);
            }
        } else {
            pageData = groupRepository.findByPrivacyIn(List.of(GroupPrivacy.PUBLIC, GroupPrivacy.CLOSED), pageable);
        }

        return toPageResponse(pageData, page, pageData.getContent().stream()
                .map(group -> buildGroupResponse(group, userId))
                .toList());
    }

    public GroupResponse getGroupById(String groupId) {
        String userId = currentUserProvider.getCurrentUserId();
        Group group = getGroupEntity(groupId);

        if (group.getPrivacy() == GroupPrivacy.PRIVATE && !group.getOwnerId().equals(userId) && !isMember(groupId, userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return buildGroupResponse(group, userId);
    }

    @Transactional
    public void addMember(String groupId, String memberUserId) {
        String userId = currentUserProvider.getCurrentUserId();
        getGroupEntity(groupId);
        checkAdminOrModeratorPermission(groupId, userId);

        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, memberUserId)) {
            throw new AppException(ErrorCode.MEMBER_ALREADY_EXISTS);
        }

        groupMemberRepository.save(GroupMember.builder()
                .groupId(groupId)
                .userId(memberUserId)
                .role(MemberRole.MEMBER)
                .joinedDate(Instant.now())
                .build());

        joinRequestRepository.findByGroupIdAndUserId(groupId, memberUserId).ifPresent(joinRequestRepository::delete);
    }

    @Transactional
    public void removeMember(String groupId, String memberUserId) {
        String userId = currentUserProvider.getCurrentUserId();
        Group group = getGroupEntity(groupId);

        if (group.getOwnerId().equals(memberUserId)) {
            throw new AppException(ErrorCode.MEMBER_CANNOT_REMOVE_OWNER);
        }

        GroupMember currentMember = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        groupMemberRepository.findByGroupIdAndUserId(groupId, memberUserId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        if (currentMember.getRole() != MemberRole.ADMIN
                && currentMember.getRole() != MemberRole.MODERATOR
                && !userId.equals(memberUserId)) {
            throw new AppException(ErrorCode.INSUFFICIENT_PERMISSION);
        }

        groupMemberRepository.deleteByGroupIdAndUserId(groupId, memberUserId);
    }

    @Transactional
    public void updateMemberRole(String groupId, String memberUserId, UpdateMemberRoleRequest request) {
        String userId = currentUserProvider.getCurrentUserId();
        Group group = getGroupEntity(groupId);
        checkAdminPermission(groupId, userId);

        if (group.getOwnerId().equals(memberUserId)) {
            throw new AppException(ErrorCode.CANNOT_CHANGE_OWNER_ROLE);
        }

        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, memberUserId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        if (request.getRole() == null) {
            throw new AppException(ErrorCode.INVALID_ROLE);
        }

        member.setRole(request.getRole());
        groupMemberRepository.save(member);
    }

    @Transactional
    public void joinGroup(String groupId, JoinGroupRequest request) {
        String userId = currentUserProvider.getCurrentUserId();
        Group group = getGroupEntity(groupId);

        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new AppException(ErrorCode.ALREADY_MEMBER);
        }

        if (!group.isRequiresApproval()) {
            groupMemberRepository.save(GroupMember.builder()
                    .groupId(groupId)
                    .userId(userId)
                    .role(MemberRole.MEMBER)
                    .joinedDate(Instant.now())
                    .build());
            return;
        }

        if (joinRequestRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new AppException(ErrorCode.JOIN_REQUEST_ALREADY_EXISTS);
        }

        joinRequestRepository.save(JoinRequest.builder()
                .groupId(groupId)
                .userId(userId)
                .status(RequestStatus.PENDING)
                .message(request.getMessage())
                .requestedDate(Instant.now())
                .build());
    }

    @Transactional
    public void leaveGroup(String groupId) {
        String userId = currentUserProvider.getCurrentUserId();
        Group group = getGroupEntity(groupId);

        if (group.getOwnerId().equals(userId)) {
            throw new AppException(ErrorCode.MEMBER_CANNOT_REMOVE_OWNER);
        }
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new AppException(ErrorCode.MEMBER_NOT_FOUND);
        }

        groupMemberRepository.deleteByGroupIdAndUserId(groupId, userId);
    }

    @Transactional
    public void processJoinRequest(String groupId, String requestId, ProcessJoinRequest request) {
        String userId = currentUserProvider.getCurrentUserId();
        getGroupEntity(groupId);
        checkAdminOrModeratorPermission(groupId, userId);

        JoinRequest joinRequest = joinRequestRepository.findById(requestId)
                .filter(candidate -> candidate.getGroupId().equals(groupId))
                .orElseThrow(() -> new AppException(ErrorCode.JOIN_REQUEST_NOT_FOUND));

        if (joinRequest.getStatus() != RequestStatus.PENDING) {
            throw new AppException(ErrorCode.JOIN_REQUEST_NOT_FOUND);
        }

        if (Boolean.TRUE.equals(request.getApprove())) {
            if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, joinRequest.getUserId())) {
                groupMemberRepository.save(GroupMember.builder()
                        .groupId(groupId)
                        .userId(joinRequest.getUserId())
                        .role(MemberRole.MEMBER)
                        .joinedDate(Instant.now())
                        .build());
            }
            joinRequest.setStatus(RequestStatus.APPROVED);
        } else {
            joinRequest.setStatus(RequestStatus.REJECTED);
        }

        joinRequest.setReviewedDate(Instant.now());
        joinRequest.setReviewedBy(userId);
        joinRequestRepository.save(joinRequest);
    }

    public PageResponse<GroupResponse> getMyGroups(int page, int size) {
        String userId = currentUserProvider.getCurrentUserId();
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdDate").descending());
        var pageData = groupRepository.findByOwnerId(userId, pageable);
        return toPageResponse(pageData, page, pageData.getContent().stream()
                .map(group -> buildGroupResponse(group, userId))
                .toList());
    }

    public PageResponse<GroupResponse> getJoinedGroups(int page, int size) {
        String userId = currentUserProvider.getCurrentUserId();
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("joinedDate").descending());
        var pageData = groupMemberRepository.findByUserId(userId, pageable);

        List<String> groupIds = pageData.getContent().stream().map(GroupMember::getGroupId).toList();
        List<Group> groups = groupIds.isEmpty() ? List.of() : groupRepository.findAllById(groupIds);
        var groupMap = groups.stream().collect(Collectors.toMap(Group::getId, group -> group));
        var groupList = pageData.getContent().stream()
                .map(member -> groupMap.get(member.getGroupId()))
                .filter(group -> group != null)
                .map(group -> buildGroupResponse(group, userId))
                .toList();

        return toPageResponse(pageData, page, groupList);
    }

    public PageResponse<GroupResponse> searchGroups(String keyword, int page, int size) {
        String userId = currentUserProvider.getCurrentUserId();
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdDate").descending());
        List<GroupPrivacy> searchablePrivacies = List.of(GroupPrivacy.PUBLIC, GroupPrivacy.CLOSED);
        var pageData = keyword != null && !keyword.trim().isEmpty()
                ? groupRepository.searchGroupsWithPrivacy(searchablePrivacies, keyword.trim(), pageable)
                : groupRepository.findByPrivacyIn(searchablePrivacies, pageable);

        return toPageResponse(pageData, page, pageData.getContent().stream()
                .map(group -> buildGroupResponse(group, userId))
                .toList());
    }

    public PageResponse<GroupMemberResponse> getGroupMembers(String groupId, String role, int page, int size) {
        String userId = currentUserProvider.getCurrentUserId();
        Group group = getGroupEntity(groupId);

        if (group.getPrivacy() == GroupPrivacy.PRIVATE && !isMember(groupId, userId) && !group.getOwnerId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("joinedDate").ascending());
        Page<GroupMember> pageData;
        if (role != null && !role.trim().isEmpty()) {
            try {
                pageData = groupMemberRepository.findByGroupIdAndRole(groupId, MemberRole.valueOf(role.toUpperCase()), pageable);
            } catch (IllegalArgumentException exception) {
                throw new AppException(ErrorCode.INVALID_ROLE);
            }
        } else {
            pageData = groupMemberRepository.findByGroupId(groupId, pageable);
        }

        return toPageResponse(pageData, page, pageData.getContent().stream()
                .map(this::buildGroupMemberResponse)
                .toList());
    }

    public PageResponse<JoinRequestResponse> getJoinRequests(String groupId, int page, int size) {
        String userId = currentUserProvider.getCurrentUserId();
        getGroupEntity(groupId);
        checkAdminOrModeratorPermission(groupId, userId);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("requestedDate").descending());
        var pageData = joinRequestRepository.findByGroupIdAndStatus(groupId, RequestStatus.PENDING, pageable);
        return toPageResponse(pageData, page, pageData.getContent().stream()
                .map(this::buildJoinRequestResponse)
                .toList());
    }

    @Transactional
    public void cancelJoinRequest(String groupId, String requestId) {
        String userId = currentUserProvider.getCurrentUserId();
        getGroupEntity(groupId);
        JoinRequest joinRequest = joinRequestRepository.findById(requestId)
                .filter(candidate -> candidate.getGroupId().equals(groupId))
                .orElseThrow(() -> new AppException(ErrorCode.JOIN_REQUEST_NOT_FOUND));

        if (!joinRequest.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        if (joinRequest.getStatus() != RequestStatus.PENDING) {
            throw new AppException(ErrorCode.JOIN_REQUEST_NOT_FOUND);
        }

        joinRequestRepository.delete(joinRequest);
    }

    public PageResponse<JoinRequestResponse> getMyJoinRequests(int page, int size) {
        String userId = currentUserProvider.getCurrentUserId();
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("requestedDate").descending());
        var pageData = joinRequestRepository.findByUserId(userId, pageable);
        return toPageResponse(pageData, page, pageData.getContent().stream()
                .map(this::buildJoinRequestResponse)
                .toList());
    }

    public boolean canPost(String groupId) {
        return canPost(groupId, currentUserProvider.getCurrentUserId());
    }

    @Override
    public boolean canPost(String groupId, String userId) {
        Group group = getGroupEntity(groupId);
        if (!group.isAllowPosting()) {
            return false;
        }
        if (group.isOnlyAdminCanPost()) {
            return isAdminOrModerator(groupId, userId);
        }
        return isMember(groupId, userId);
    }

    public boolean canViewPosts(String groupId) {
        return canView(groupId, currentUserProvider.getCurrentUserId());
    }

    @Override
    public boolean canView(String groupId, String userId) {
        if (groupId == null || userId == null) {
            return false;
        }

        return groupRepository.findById(groupId)
                .map(group -> switch (group.getPrivacy()) {
                    case PUBLIC, CLOSED -> true;
                    case PRIVATE -> isMember(groupId, userId) || group.getOwnerId().equals(userId);
                })
                .orElse(false);
    }

    @Override
    public boolean exists(String groupId) {
        return groupRepository.existsById(groupId);
    }

    public boolean checkGroupExists(String groupId) {
        return exists(groupId);
    }

    @Override
    public GroupResponse getGroup(String groupId) {
        return buildGroupResponse(getGroupEntity(groupId), currentUserProvider.getCurrentUserId());
    }

    private Group getGroupEntity(String groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));
    }

    private void validateUploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }
    }

    private boolean isMember(String groupId, String userId) {
        return groupMemberRepository.existsByGroupIdAndUserId(groupId, userId);
    }

    private boolean isAdminOrModerator(String groupId, String userId) {
        return groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .map(member -> member.getRole() == MemberRole.ADMIN || member.getRole() == MemberRole.MODERATOR)
                .orElse(false);
    }

    private boolean isAdmin(String groupId, String userId) {
        return groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .map(member -> member.getRole() == MemberRole.ADMIN)
                .orElse(false);
    }

    private void checkAdminPermission(String groupId, String userId) {
        if (!isAdmin(groupId, userId)) {
            throw new AppException(ErrorCode.INSUFFICIENT_PERMISSION);
        }
    }

    private void checkAdminOrModeratorPermission(String groupId, String userId) {
        if (!isAdminOrModerator(groupId, userId)) {
            throw new AppException(ErrorCode.INSUFFICIENT_PERMISSION);
        }
    }

    private void checkOwnerOrAdmin(Group group, String userId) {
        if (group.getOwnerId().equals(userId)) {
            return;
        }

        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(group.getId(), userId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_OWNER));
        if (member.getRole() != MemberRole.ADMIN) {
            throw new AppException(ErrorCode.GROUP_NOT_OWNER);
        }
    }

    private ProfileResponse getUserProfile(String userId) {
        try {
            return profileQueryPort.getProfileByUserId(userId);
        } catch (Exception exception) {
            log.warn("Failed to get profile for group user {}: {}", userId, exception.getMessage());
            return null;
        }
    }

    private GroupResponse buildGroupResponse(Group group, String currentUserId) {
        GroupResponse response = groupMapper.toGroupResponse(group);
        ProfileResponse ownerProfile = getUserProfile(group.getOwnerId());
        if (ownerProfile != null) {
            response.setOwnerName(getDisplayName(
                    ownerProfile.getFirstName(), ownerProfile.getLastName(), ownerProfile.getUsername()));
            response.setOwnerAvatar(ownerProfile.getAvatar());
        }

        response.setMemberCount(groupMemberRepository.countByGroupId(group.getId()));
        if (isAdminOrModerator(group.getId(), currentUserId) || group.getOwnerId().equals(currentUserId)) {
            response.setPendingRequestCount(joinRequestRepository.countByGroupIdAndStatus(group.getId(), RequestStatus.PENDING));
        }
        response.setCreatedDate(dateTimeFormatter.format(group.getCreatedDate()));
        response.setModifiedDate(dateTimeFormatter.format(group.getModifiedDate()));
        response.setMember(isMember(group.getId(), currentUserId));
        if (response.isMember()) {
            groupMemberRepository.findByGroupIdAndUserId(group.getId(), currentUserId)
                    .ifPresent(member -> response.setMemberRole(MemberRoleResponse.builder()
                            .role(member.getRole().name())
                            .joinedDate(dateTimeFormatter.format(member.getJoinedDate()))
                            .build()));
        }
        return response;
    }

    private GroupMemberResponse buildGroupMemberResponse(GroupMember member) {
        GroupMemberResponse response = groupMemberMapper.toGroupMemberResponse(member);
        ProfileResponse profile = getUserProfile(member.getUserId());
        if (profile != null) {
            response.setUsername(getDisplayName(profile.getFirstName(), profile.getLastName(), profile.getUsername()));
            response.setAvatar(profile.getAvatar());
        }
        response.setJoinedDate(dateTimeFormatter.format(member.getJoinedDate()));
        return response;
    }

    private JoinRequestResponse buildJoinRequestResponse(JoinRequest joinRequest) {
        JoinRequestResponse response = joinRequestMapper.toJoinRequestResponse(joinRequest);
        groupRepository.findById(joinRequest.getGroupId()).ifPresent(group -> response.setGroupName(group.getName()));
        ProfileResponse profile = getUserProfile(joinRequest.getUserId());
        if (profile != null) {
            response.setUsername(getDisplayName(profile.getFirstName(), profile.getLastName(), profile.getUsername()));
            response.setAvatar(profile.getAvatar());
        }
        response.setRequestedDate(dateTimeFormatter.format(joinRequest.getRequestedDate()));
        response.setReviewedDate(dateTimeFormatter.format(joinRequest.getReviewedDate()));
        return response;
    }

    private String getDisplayName(String firstName, String lastName, String username) {
        if (firstName != null && !firstName.trim().isEmpty() && lastName != null && !lastName.trim().isEmpty()) {
            return (firstName.trim() + " " + lastName.trim()).trim();
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            return lastName.trim();
        }
        if (firstName != null && !firstName.trim().isEmpty()) {
            return firstName.trim();
        }
        return username != null ? username : "";
    }

    private <T> PageResponse<T> toPageResponse(Page<?> pageData, int page, List<T> content) {
        return PageResponse.<T>builder()
                .content(content)
                .page(page)
                .size(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .build();
    }
}
