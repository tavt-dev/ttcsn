package com.friendify.app.group.entity;

import java.time.Instant;

import com.friendify.app.group.enums.RequestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "join_request", uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "user_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JoinRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "group_id", nullable = false)
    String groupId;

    @Column(name = "user_id", nullable = false)
    String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    RequestStatus status;

    @Column(length = 1000)
    String message;

    Instant requestedDate;
    Instant reviewedDate;
    String reviewedBy;
}
