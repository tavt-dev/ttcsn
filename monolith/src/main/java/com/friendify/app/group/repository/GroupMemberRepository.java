package com.friendify.app.group.repository;

import java.util.List;
import java.util.Optional;

import com.friendify.app.group.entity.GroupMember;
import com.friendify.app.group.enums.MemberRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, String> {
    Optional<GroupMember> findByGroupIdAndUserId(String groupId, String userId);

    boolean existsByGroupIdAndUserId(String groupId, String userId);

    Page<GroupMember> findByGroupId(String groupId, Pageable pageable);

    Page<GroupMember> findByGroupIdAndRole(String groupId, MemberRole role, Pageable pageable);

    Page<GroupMember> findByUserId(String userId, Pageable pageable);

    List<GroupMember> findByGroupId(String groupId);

    List<GroupMember> findByUserId(String userId);

    long countByGroupId(String groupId);

    void deleteByGroupIdAndUserId(String groupId, String userId);

    void deleteAllByGroupId(String groupId);
}
