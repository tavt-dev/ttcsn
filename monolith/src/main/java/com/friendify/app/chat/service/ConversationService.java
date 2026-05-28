package com.friendify.app.chat.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.friendify.app.chat.dto.request.AddAdminRequest;
import com.friendify.app.chat.dto.request.AddParticipantRequest;
import com.friendify.app.chat.dto.request.ConversationRequest;
import com.friendify.app.chat.dto.request.UpdateConversationRequest;
import com.friendify.app.chat.dto.response.ConversationResponse;
import com.friendify.app.chat.entity.Conversation;
import com.friendify.app.chat.entity.ParticipantInfo;
import com.friendify.app.chat.enums.ParticipantRole;
import com.friendify.app.chat.enums.TypeConversation;
import com.friendify.app.chat.mapper.ConversationMapper;
import com.friendify.app.chat.repository.ConversationRepository;
import com.friendify.app.profile.dto.response.ProfileResponse;
import com.friendify.app.profile.port.ProfileQueryPort;
import com.friendify.app.shared.exception.AppException;
import com.friendify.app.shared.exception.ErrorCode;
import com.friendify.app.shared.security.CurrentUserProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationService {

    static String PARTICIPANT_HASH_DELIMITER = "_";
    static int MIN_GROUP_PARTICIPANTS_AFTER_LEAVE = 2;

    ConversationRepository conversationRepository;
    ProfileQueryPort profileQueryPort;
    ConversationMapper conversationMapper;
    CurrentUserProvider currentUserProvider;

    public List<ConversationResponse> myConversations() {
        String userId = currentUserProvider.getCurrentUserId();
        return conversationRepository.findAllByParticipantIdsContains(userId).stream()
                .map(this::toConversationResponse)
                .toList();
    }

    @Transactional
    public ConversationResponse create(ConversationRequest request) {
        String currentUserId = currentUserProvider.getCurrentUserId();
        validateConversationRequest(request);

        ProfileResponse currentUserInfo = getProfileOrThrow(currentUserId);
        List<String> otherParticipantIds = getOtherParticipantIds(request.getParticipantIds(), currentUserId);
        List<ProfileResponse> participantProfiles = getProfilesOrThrow(otherParticipantIds);
        TypeConversation typeConversation =
                otherParticipantIds.size() == 1 ? TypeConversation.DIRECT : TypeConversation.GROUP;

        List<ParticipantInfo> participantInfos =
                buildParticipantInfos(currentUserInfo, participantProfiles, typeConversation, true);
        String userIdHash = generateParticipantHash(participantInfos);

        Conversation conversation = findOrCreateConversation(
                typeConversation, participantInfos, userIdHash, request.getConversationName());
        return toConversationResponse(conversation);
    }

    public ConversationResponse getById(String conversationId) {
        String userId = currentUserProvider.getCurrentUserId();
        Conversation conversation = findConversationOrThrow(conversationId);
        validateParticipantAccess(conversation, userId);
        return toConversationResponse(conversation);
    }

    @Transactional
    public ConversationResponse updateConversation(String conversationId, UpdateConversationRequest request) {
        String userId = currentUserProvider.getCurrentUserId();
        Conversation conversation = findConversationOrThrow(conversationId);
        validateParticipantAccess(conversation, userId);
        if (conversation.getTypeConversation() == TypeConversation.GROUP) {
            validateAdminPermission(conversation, userId);
        }

        updateConversationDetails(conversation, request);
        return toConversationResponse(conversationRepository.save(conversation));
    }

    @Transactional
    public void deleteConversation(String conversationId) {
        String userId = currentUserProvider.getCurrentUserId();
        Conversation conversation = findConversationOrThrow(conversationId);
        validateParticipantAccess(conversation, userId);
        if (conversation.getTypeConversation() == TypeConversation.GROUP) {
            validateAdminPermission(conversation, userId);
        }
        conversationRepository.delete(conversation);
    }

    @Transactional
    public ConversationResponse addParticipants(String conversationId, AddParticipantRequest request) {
        String userId = currentUserProvider.getCurrentUserId();
        Conversation conversation = findConversationOrThrow(conversationId);
        validateGroupConversation(conversation);
        validateParticipantAccess(conversation, userId);
        validateAdminPermission(conversation, userId);
        validateNoDuplicateParticipantIds(request.getParticipantIds());

        Set<String> existingParticipantIds = conversation.getParticipants().stream()
                .map(ParticipantInfo::getUserId)
                .collect(Collectors.toSet());
        List<String> duplicateIds = request.getParticipantIds().stream()
                .filter(existingParticipantIds::contains)
                .toList();
        if (!duplicateIds.isEmpty()) {
            throw new AppException(ErrorCode.PARTICIPANT_ALREADY_EXISTS);
        }

        List<String> newParticipantIds = new ArrayList<>(request.getParticipantIds());
        if (newParticipantIds.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        List<ParticipantInfo> updatedParticipants = new ArrayList<>(conversation.getParticipants());
        updatedParticipants.addAll(buildParticipantInfosFromProfiles(getProfilesOrThrow(newParticipantIds), false));
        conversation.setParticipants(updatedParticipants);
        conversation.setModifiedDate(Instant.now());
        return toConversationResponse(conversationRepository.save(conversation));
    }

    @Transactional
    public ConversationResponse removeParticipant(String conversationId, String participantId) {
        String userId = currentUserProvider.getCurrentUserId();
        Conversation conversation = findConversationOrThrow(conversationId);
        validateGroupConversation(conversation);
        validateParticipantAccess(conversation, userId);
        validateAdminPermission(conversation, userId);
        validateNotSelf(participantId, userId);

        List<ParticipantInfo> updatedParticipants = conversation.getParticipants().stream()
                .filter(participant -> !participant.getUserId().equals(participantId))
                .collect(Collectors.toList());
        if (updatedParticipants.size() == conversation.getParticipants().size()) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        conversation.setParticipants(updatedParticipants);
        conversation.setModifiedDate(Instant.now());
        return toConversationResponse(conversationRepository.save(conversation));
    }

    @Transactional
    public ConversationResponse promoteToAdmin(String conversationId, AddAdminRequest request) {
        String userId = currentUserProvider.getCurrentUserId();
        Conversation conversation = findConversationOrThrow(conversationId);
        validateGroupConversation(conversation);
        validateParticipantAccess(conversation, userId);
        validateAdminPermission(conversation, userId);
        validateNoDuplicateParticipantIds(request.getParticipantIds());

        Set<String> participantIds = conversation.getParticipants().stream()
                .map(ParticipantInfo::getUserId)
                .collect(Collectors.toSet());
        if (request.getParticipantIds().stream().anyMatch(id -> !participantIds.contains(id))) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        conversation.getParticipants().forEach(participant -> {
            if (request.getParticipantIds().contains(participant.getUserId())) {
                participant.setRole(ParticipantRole.ADMIN);
            }
        });
        conversation.setModifiedDate(Instant.now());
        return toConversationResponse(conversationRepository.save(conversation));
    }

    @Transactional
    public ConversationResponse demoteFromAdmin(String conversationId, String participantId) {
        String userId = currentUserProvider.getCurrentUserId();
        Conversation conversation = findConversationOrThrow(conversationId);
        validateGroupConversation(conversation);
        validateParticipantAccess(conversation, userId);
        validateAdminPermission(conversation, userId);
        validateNotSelf(participantId, userId);

        ParticipantInfo targetParticipant = conversation.getParticipants().stream()
                .filter(participant -> participant.getUserId().equals(participantId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (targetParticipant.getRole() != ParticipantRole.ADMIN) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }
        long adminCount = conversation.getParticipants().stream()
                .filter(participant -> participant.getRole() == ParticipantRole.ADMIN)
                .count();
        if (adminCount <= 1) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        targetParticipant.setRole(ParticipantRole.MEMBER);
        conversation.setModifiedDate(Instant.now());
        return toConversationResponse(conversationRepository.save(conversation));
    }

    @Transactional
    public void leaveConversation(String conversationId) {
        String userId = currentUserProvider.getCurrentUserId();
        Conversation conversation = findConversationOrThrow(conversationId);
        validateParticipantAccess(conversation, userId);

        if (conversation.getTypeConversation() == TypeConversation.DIRECT) {
            conversationRepository.delete(conversation);
            return;
        }

        List<ParticipantInfo> updatedParticipants = conversation.getParticipants().stream()
                .filter(participant -> !participant.getUserId().equals(userId))
                .collect(Collectors.toList());
        if (updatedParticipants.size() < MIN_GROUP_PARTICIPANTS_AFTER_LEAVE) {
            conversationRepository.delete(conversation);
        } else {
            conversation.setParticipants(updatedParticipants);
            conversation.setModifiedDate(Instant.now());
            conversationRepository.save(conversation);
        }
    }

    Conversation findConversationOrThrow(String conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));
    }

    private ProfileResponse getProfileOrThrow(String userId) {
        try {
            return profileQueryPort.getProfileByUserId(userId);
        } catch (RuntimeException exception) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
    }

    private List<ProfileResponse> getProfilesOrThrow(List<String> userIds) {
        if (userIds.isEmpty()) {
            return List.of();
        }
        List<ProfileResponse> profiles = profileQueryPort.getProfilesByUserIds(userIds);
        if (profiles == null || profiles.isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        return profiles;
    }

    private void validateConversationRequest(ConversationRequest request) {
        if (request.getParticipantIds() == null || request.getParticipantIds().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_CONVERSATION_TYPE);
        }
    }

    private List<String> getOtherParticipantIds(List<String> participantIds, String currentUserId) {
        List<String> otherIds = new ArrayList<>(participantIds);
        otherIds.remove(currentUserId);
        return otherIds;
    }

    private List<ParticipantInfo> buildParticipantInfos(
            ProfileResponse currentUser,
            List<ProfileResponse> otherProfiles,
            TypeConversation typeConversation,
            boolean isCreator) {
        List<ParticipantInfo> participantInfos = new ArrayList<>();
        if (typeConversation == TypeConversation.DIRECT) {
            participantInfos.add(buildParticipantInfo(currentUser, ParticipantRole.ADMIN));
            participantInfos.addAll(buildParticipantInfosFromProfiles(otherProfiles, true));
        } else {
            ParticipantRole creatorRole = isCreator ? ParticipantRole.ADMIN : ParticipantRole.MEMBER;
            participantInfos.add(buildParticipantInfo(currentUser, creatorRole));
            participantInfos.addAll(buildParticipantInfosFromProfiles(otherProfiles, false));
        }
        return participantInfos;
    }

    private List<ParticipantInfo> buildParticipantInfosFromProfiles(List<ProfileResponse> profiles, boolean isAdmin) {
        ParticipantRole role = isAdmin ? ParticipantRole.ADMIN : ParticipantRole.MEMBER;
        return profiles.stream()
                .map(profile -> buildParticipantInfo(profile, role))
                .toList();
    }

    private ParticipantInfo buildParticipantInfo(ProfileResponse profile, ParticipantRole role) {
        return ParticipantInfo.builder()
                .userId(profile.getUserId())
                .username(profile.getUsername())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .avatar(profile.getAvatar())
                .role(role)
                .build();
    }

    private String generateParticipantHash(List<ParticipantInfo> participantInfos) {
        List<String> sortedIds = participantInfos.stream()
                .map(ParticipantInfo::getUserId)
                .sorted()
                .toList();
        StringJoiner stringJoiner = new StringJoiner(PARTICIPANT_HASH_DELIMITER);
        sortedIds.forEach(stringJoiner::add);
        return stringJoiner.toString();
    }

    private Conversation findOrCreateConversation(
            TypeConversation typeConversation,
            List<ParticipantInfo> participantInfos,
            String userIdHash,
            String conversationName) {
        return conversationRepository.findByParticipantsHash(userIdHash)
                .filter(conversation -> conversation.getTypeConversation() == typeConversation)
                .map(this::normalizeDirectConversation)
                .orElseGet(() -> createNewConversation(typeConversation, participantInfos, userIdHash, conversationName));
    }

    private Conversation normalizeDirectConversation(Conversation conversation) {
        if (conversation.getTypeConversation() == TypeConversation.DIRECT) {
            conversation.getParticipants().forEach(participant -> participant.setRole(ParticipantRole.ADMIN));
            conversation.setModifiedDate(Instant.now());
            return conversationRepository.save(conversation);
        }
        return conversation;
    }

    private Conversation createNewConversation(
            TypeConversation typeConversation,
            List<ParticipantInfo> participantInfos,
            String userIdHash,
            String conversationName) {
        return conversationRepository.save(Conversation.builder()
                .typeConversation(typeConversation)
                .participants(participantInfos)
                .participantsHash(userIdHash)
                .conversationName(conversationName)
                .createdDate(Instant.now())
                .modifiedDate(Instant.now())
                .build());
    }

    private void validateGroupConversation(Conversation conversation) {
        if (conversation.getTypeConversation() != TypeConversation.GROUP) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void validateNotSelf(String participantId, String userId) {
        if (participantId.equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void validateNoDuplicateParticipantIds(List<String> participantIds) {
        Set<String> uniqueIds = new HashSet<>(participantIds);
        if (uniqueIds.size() != participantIds.size()) {
            throw new AppException(ErrorCode.DUPLICATE_PARTICIPANT_IDS);
        }
    }

    private void validateAdminPermission(Conversation conversation, String userId) {
        if (conversation.getTypeConversation() == TypeConversation.DIRECT) {
            return;
        }
        boolean isAdmin = conversation.getParticipants().stream()
                .anyMatch(participant -> participant.getUserId().equals(userId)
                        && participant.getRole() == ParticipantRole.ADMIN);
        if (!isAdmin) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void updateConversationDetails(Conversation conversation, UpdateConversationRequest request) {
        if (request.getConversationName() != null && !request.getConversationName().trim().isEmpty()) {
            conversation.setConversationName(request.getConversationName());
        }
        if (request.getConversationAvatar() != null) {
            conversation.setConversationAvatar(request.getConversationAvatar());
        }
        conversation.setModifiedDate(Instant.now());
    }

    private ConversationResponse toConversationResponse(Conversation conversation) {
        String currentUserId = currentUserProvider.getCurrentUserId();
        ConversationResponse response = conversationMapper.toConversationResponse(conversation);
        if (conversation.getTypeConversation() == TypeConversation.GROUP) {
            if (conversation.getConversationName() != null) {
                response.setConversationName(conversation.getConversationName());
            }
            if (conversation.getConversationAvatar() != null) {
                response.setConversationAvatar(conversation.getConversationAvatar());
            }
        } else {
            conversation.getParticipants().stream()
                    .filter(participant -> !participant.getUserId().equals(currentUserId))
                    .findFirst()
                    .ifPresent(participant -> {
                        response.setConversationName(displayName(participant));
                        response.setConversationAvatar(participant.getAvatar());
                    });
        }
        return response;
    }

    private String displayName(ParticipantInfo participant) {
        if (hasText(participant.getFirstName()) && hasText(participant.getLastName())) {
            return (participant.getFirstName().trim() + " " + participant.getLastName().trim()).trim();
        }
        if (hasText(participant.getLastName())) {
            return participant.getLastName().trim();
        }
        if (hasText(participant.getFirstName())) {
            return participant.getFirstName().trim();
        }
        return participant.getUsername() != null ? participant.getUsername() : "";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    void validateParticipantAccess(Conversation conversation, String userId) {
        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(participant -> participant.getUserId().equals(userId));
        if (!isParticipant) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }
}
