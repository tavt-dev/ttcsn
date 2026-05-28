package com.friendify.app.group.repository;

import java.util.List;
import java.util.Optional;

import com.friendify.app.group.entity.Group;
import com.friendify.app.group.enums.GroupPrivacy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, String> {
    Page<Group> findByOwnerId(String ownerId, Pageable pageable);

    Page<Group> findByPrivacy(GroupPrivacy privacy, Pageable pageable);

    Optional<Group> findByIdAndOwnerId(String id, String ownerId);

    Page<Group> findByPrivacyIn(List<GroupPrivacy> privacyTypes, Pageable pageable);

    @Query("""
            SELECT g FROM FriendifyGroup g
            WHERE g.privacy IN :privacyTypes
              AND (
                LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(g.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
    Page<Group> searchGroupsWithPrivacy(
            @Param("privacyTypes") List<GroupPrivacy> privacyTypes,
            @Param("keyword") String keyword,
            Pageable pageable);
}
