package uz.aifu.lms.iam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * Teacher-specific attributes. {@code officeHours} is stored as free text on purpose: the syllabus
 * states it in prose ("Seshanba, Payshanba 14.00-16.00") and there is no timetable system to
 * structure it against yet (PENDING-INPUTS.md §4).
 */
@Entity
@Table(name = "teacher_profiles")
public class TeacherProfile {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "employee_number", unique = true)
    private String employeeNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "academic_title")
    private String academicTitle;

    @Column(name = "academic_degree")
    private String academicDegree;

    @Column(name = "office_hours")
    private String officeHours;

    protected TeacherProfile() {}

    public TeacherProfile(
            User user, String employeeNumber, Department department, String academicTitle) {
        this.user = user;
        this.employeeNumber = employeeNumber;
        this.department = department;
        this.academicTitle = academicTitle;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public Department getDepartment() {
        return department;
    }

    public String getAcademicTitle() {
        return academicTitle;
    }

    public String getAcademicDegree() {
        return academicDegree;
    }

    public String getOfficeHours() {
        return officeHours;
    }
}
