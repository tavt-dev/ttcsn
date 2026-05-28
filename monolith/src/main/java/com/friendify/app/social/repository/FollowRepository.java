package com.friendify.app.social.repository;

import java.util.List;
import java.util.Optional;

import com.friendify.app.social.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, String> {
    Optional<Follow> findByFollowerIdAndFollowingId(String followerId, String followingId);

    boolean existsByFollowerIdAndFollowingId(String followerId, String followingId);

    @Query("SELECT f FROM Follow f WHERE f.followerId = :userId")
    Page<Follow> findFollowingByUserId(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT f FROM Follow f WHERE f.followingId = :userId")
    Page<Follow> findFollowersByUserId(@Param("userId") String userId, Pageable pageable);

    long countByFollowerId(String userId);

    long countByFollowingId(String userId);

    @Query("SELECT f.followingId FROM Follow f WHERE f.followerId = :userId")
    List<String> findFollowingIdsByUserId(@Param("userId") String userId);
}
