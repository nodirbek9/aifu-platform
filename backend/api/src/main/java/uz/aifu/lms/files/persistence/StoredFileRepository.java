package uz.aifu.lms.files.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.aifu.lms.files.domain.StoredFile;

public interface StoredFileRepository extends JpaRepository<StoredFile, UUID> {}
