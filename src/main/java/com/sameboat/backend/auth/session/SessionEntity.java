package com.sameboat.backend.auth.session;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.AccessLevel;

/**
 * JPA entity representing an authenticated browser/session token. Stores user linkage,
 * creation, last-seen and expiry timestamps (UTC). Expiry enforcement is handled in
 * the service layer rather than a DB constraint for portability.
 */
@Entity
@Table(name = "sessions")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SessionEntity {

    /** Unique session identifier (also used as cookie value). */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(columnDefinition = "uuid")
    @Setter(AccessLevel.NONE)
    private UUID id;

    /** Owning user id. */
    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    /** Timestamp the session was created. */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private OffsetDateTime createdAt;

    /** Last time the session was observed/"touched" by activity. */
    @Column(name = "last_seen_at", nullable = false)
    private OffsetDateTime lastSeenAt;

    /** Absolute expiry timestamp after which session is invalid. */
    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @PrePersist
    void prePersist() {
        var now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        if (lastSeenAt == null) lastSeenAt = createdAt;
    }
}
