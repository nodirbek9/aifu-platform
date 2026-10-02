package uz.aifu.lms.curriculum.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uz.aifu.lms.common.util.Guards;

/**
 * DAS1110: "mustaqil ish topshiriqlari variativ tavsifga ega bo'lishi lozim" (V007). Assignment to
 * a student is deterministic: {@code hash(studentUserId, activityId) % variantCount} — computed
 * where the variant is actually resolved (the future {@code coding}/{@code assessment} handler),
 * not stored here.
 */
@Entity
@Table(name = "activity_variants")
public class ActivityVariant {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false, updatable = false)
    private Activity activity;

    @Column(name = "variant_no", nullable = false, updatable = false)
    private int variantNo;

    @Column(name = "reference_id")
    private UUID referenceId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false)
    private String payload = "{}";

    protected ActivityVariant() {}

    public ActivityVariant(Activity activity, int variantNo, UUID referenceId, String payloadJson) {
        this.activity = Guards.required(activity, "activity");
        Guards.require(variantNo >= 1, "variantNo must be >= 1");
        this.variantNo = variantNo;
        this.referenceId = referenceId;
        this.payload = payloadJson == null ? "{}" : payloadJson;
    }

    public void update(UUID referenceId, String payloadJson) {
        this.referenceId = referenceId;
        this.payload = payloadJson == null ? "{}" : payloadJson;
    }

    public UUID getId() {
        return id;
    }

    public Activity getActivity() {
        return activity;
    }

    public int getVariantNo() {
        return variantNo;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public String getPayload() {
        return payload;
    }
}
