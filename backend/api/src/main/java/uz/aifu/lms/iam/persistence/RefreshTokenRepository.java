package uz.aifu.lms.iam.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.iam.domain.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /** Lookup is by HASH; the raw token never reaches the database. */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findAllByFamilyId(UUID familyId);

    /**
     * {@code REQUIRES_NEW} is load-bearing, not decorative: every caller invokes this and then
     * throws a {@code DomainException} to reject the request (replay detected / account no longer
     * active). Spring rolls back the whole physical transaction on that unchecked exception, which
     * would silently undo the revocation this call just made — defeating the entire replay-defense
     * guarantee (ADR-003). Committing independently, before the caller's exception can touch it, is
     * the only way the revocation survives.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            """
            update RefreshToken t set t.revokedAt = :now, t.revokedReason = :reason
            where t.familyId = :familyId and t.revokedAt is null
            """)
    int revokeFamily(
            @Param("familyId") UUID familyId,
            @Param("reason") RefreshToken.RevocationReason reason,
            @Param("now") Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            """
            update RefreshToken t set t.revokedAt = :now, t.revokedReason = :reason
            where t.user.id = :userId and t.revokedAt is null
            """)
    int revokeAllForUser(
            @Param("userId") UUID userId,
            @Param("reason") RefreshToken.RevocationReason reason,
            @Param("now") Instant now);

    /** Housekeeping: expired rows have no security value once past their TTL. */
    @Modifying
    @Query("delete from RefreshToken t where t.expiresAt < :cutoff")
    int deleteExpired(@Param("cutoff") Instant cutoff);
}
