package uz.aifu.lms.coding.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.coding.domain.ProgrammingLanguage;

public interface ProgrammingLanguageRepository extends JpaRepository<ProgrammingLanguage, String> {

    List<ProgrammingLanguage> findByEnabledTrue();
}
