package com.friendify.app.chat.repository;

import java.util.List;
import java.util.Optional;

import com.friendify.app.chat.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {
    Optional<Conversation> findByParticipantsHash(String hash);

    @Query("SELECT DISTINCT c FROM Conversation c JOIN c.participants p WHERE p.userId = :userId")
    List<Conversation> findAllByParticipantIdsContains(@Param("userId") String userId);
}
