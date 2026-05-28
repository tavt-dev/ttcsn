package com.friendify.app.social.repository;

import java.util.List;
import java.util.Optional;

import com.friendify.app.social.entity.Friendship;
import com.friendify.app.social.enums.FriendshipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, String> {
    Optional<Friendship> findByUserIdAndFriendId(String userId, String friendId);

    Optional<Friendship> findByUserIdAndFriendIdAndStatus(String userId, String friendId, FriendshipStatus status);

    boolean existsByUserIdAndFriendId(String userId, String friendId);

    @Query("SELECT f FROM Friendship f WHERE (f.userId = :userId OR f.friendId = :userId) AND f.status = :status")
    Page<Friendship> findFriendshipsByUserIdAndStatus(
            @Param("userId") String userId, @Param("status") FriendshipStatus status, Pageable pageable);

    @Query("SELECT f FROM Friendship f WHERE f.userId = :userId AND f.status = :status")
    Page<Friendship> findSentFriendRequests(
            @Param("userId") String userId, @Param("status") FriendshipStatus status, Pageable pageable);

    @Query("SELECT f FROM Friendship f WHERE f.friendId = :userId AND f.status = :status")
    Page<Friendship> findReceivedFriendRequests(
            @Param("userId") String userId, @Param("status") FriendshipStatus status, Pageable pageable);

    @Query("SELECT COUNT(f) FROM Friendship f WHERE f.friendId = :userId AND f.status = :status")
    long countReceivedFriendRequests(@Param("userId") String userId, @Param("status") FriendshipStatus status);

    @Query("SELECT COUNT(f) FROM Friendship f WHERE f.userId = :userId AND f.status = :status")
    long countSentFriendRequests(@Param("userId") String userId, @Param("status") FriendshipStatus status);

    @Query("SELECT COUNT(f) FROM Friendship f WHERE (f.userId = :userId OR f.friendId = :userId) AND f.status = 'ACCEPTED'")
    long countFriends(@Param("userId") String userId);

    @Query("SELECT f FROM Friendship f WHERE (f.userId = :userId OR f.friendId = :userId) AND f.status = 'ACCEPTED'")
    List<Friendship> findAllFriends(@Param("userId") String userId);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friendship f "
            + "WHERE ((f.userId = :userId1 AND f.friendId = :userId2) OR (f.userId = :userId2 AND f.friendId = :userId1)) "
            + "AND f.status = 'ACCEPTED'")
    boolean areFriends(@Param("userId1") String userId1, @Param("userId2") String userId2);

    @Modifying
    @Transactional
    @Query("DELETE FROM Friendship f WHERE (f.userId = :userId AND f.friendId = :friendId) OR (f.userId = :friendId AND f.friendId = :userId)")
    void deleteFriendship(@Param("userId") String userId, @Param("friendId") String friendId);

    @Query("SELECT DISTINCT CASE "
            + "WHEN f1.userId = :userId1 THEN f1.friendId "
            + "ELSE f1.userId "
            + "END "
            + "FROM Friendship f1 "
            + "WHERE f1.status = 'ACCEPTED' "
            + "AND (f1.userId = :userId1 OR f1.friendId = :userId1) "
            + "AND EXISTS ("
            + "  SELECT 1 FROM Friendship f2 "
            + "  WHERE f2.status = 'ACCEPTED' "
            + "  AND (f2.userId = :userId2 OR f2.friendId = :userId2) "
            + "  AND (CASE WHEN f1.userId = :userId1 THEN f1.friendId ELSE f1.userId END = "
            + "       CASE WHEN f2.userId = :userId2 THEN f2.friendId ELSE f2.userId END)"
            + ")")
    List<String> findMutualFriendIds(@Param("userId1") String userId1, @Param("userId2") String userId2);
}
