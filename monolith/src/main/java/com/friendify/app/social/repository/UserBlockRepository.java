package com.friendify.app.social.repository;

import java.util.List;
import java.util.Optional;

import com.friendify.app.social.entity.UserBlock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, String> {
    Optional<UserBlock> findByBlockerIdAndBlockedId(String blockerId, String blockedId);

    boolean existsByBlockerIdAndBlockedId(String blockerId, String blockedId);

    @Query("SELECT ub FROM UserBlock ub WHERE ub.blockerId = :userId")
    Page<UserBlock> findBlockedUsersByUserId(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(ub) > 0 THEN true ELSE false END FROM UserBlock ub "
            + "WHERE (ub.blockerId = :userId1 AND ub.blockedId = :userId2) OR (ub.blockerId = :userId2 AND ub.blockedId = :userId1)")
    boolean isBlocked(@Param("userId1") String userId1, @Param("userId2") String userId2);

    @Query("SELECT ub.blockedId FROM UserBlock ub WHERE ub.blockerId = :userId")
    List<String> findBlockedUserIds(@Param("userId") String userId);

    long countByBlockerId(String blockerId);
}
