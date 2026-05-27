package com.friendify.app.profile.repository;

import java.util.List;
import java.util.Optional;

import com.friendify.app.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, String> {
    Optional<Profile> findByUserId(String userId);

    List<Profile> findByUsernameContainingIgnoreCase(String username);

    List<Profile> findAllByUserIdIn(List<String> userIds);

    @Query("SELECT p FROM Profile p WHERE "
            + "LOWER(p.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(CONCAT(p.firstName, ' ', p.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(p.bio) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(p.country) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(p.city) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Profile> searchByKeyword(@Param("keyword") String keyword);
}
