package uz.aifu.lms.scheduling.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import uz.aifu.lms.common.util.Guards;

/** A non-teaching day: classes that fall on it are shown as not held. */
@Entity
@Table(name = "holidays")
public class Holiday {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "on_date", nullable = false, unique = true)
    private LocalDate onDate;

    @Column(name = "name", nullable = false)
    private String name;

    protected Holiday() {}

    public Holiday(LocalDate onDate, String name) {
        this.onDate = Guards.required(onDate, "onDate");
        this.name = Guards.requiredText(name, "name").trim();
    }

    public UUID getId() {
        return id;
    }

    public LocalDate getOnDate() {
        return onDate;
    }

    public String getName() {
        return name;
    }
}
