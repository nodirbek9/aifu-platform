package uz.aifu.lms.coding.app;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.coding.persistence.CodingTaskRepository;
import uz.aifu.lms.iam.app.access.CodingLocator;

/** Real implementation of {@link CodingLocator} (ADR-013). */
@Component
public class CodingLocatorAdapter implements CodingLocator {

    private final CodingTaskRepository codingTasks;

    public CodingLocatorAdapter(CodingTaskRepository codingTasks) {
        this.codingTasks = codingTasks;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> courseIdOfCodingTask(UUID codingTaskId) {
        return codingTasks.findCourseIdById(codingTaskId);
    }
}
