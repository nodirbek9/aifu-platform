package uz.aifu.lms.iam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import uz.aifu.lms.common.domain.BaseEntity;

/** AIFU "kafedra". Both supplied syllabi belong to "Aniq fanlar". */
@Entity
@Table(name = "departments")
public class Department extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "name_en")
    private String nameEn;

    protected Department() {}

    public Department(String code, String name, String nameEn) {
        this.code = code;
        this.name = name;
        this.nameEn = nameEn;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getNameEn() {
        return nameEn;
    }
}
