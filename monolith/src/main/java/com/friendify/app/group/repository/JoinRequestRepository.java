package com.friendify.app.group.repository;

import java.util.Optional;

import com.friendify.app.group.entity.JoinRequest;
import com.friendify.app.group.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JoinRequestRepository extends JpaRepository<JoinRequest, String> {
    Optional<JoinRequest> findByGroupIdAndUserId(String groupId, String userId);

    boolean existsByGroupIdAndUserId(String groupId, String userId);

    Page<JoinRequest> findByGroupId(String groupId, Pageable pageable);

    Page<JoinRequest> findByGroupIdAndStatus(String groupId, RequestStatus status, Pageable pageable);

    Page<JoinRequest> findByUserId(String userId, Pageable pageable);

    void deleteByGroupIdAndUserId(String groupId, String userId);

    void deleteAllByGroupId(String groupId);

    long countByGroupIdAndStatus(String groupId, RequestStatus status);
}
