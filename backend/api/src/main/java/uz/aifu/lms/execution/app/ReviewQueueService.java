package uz.aifu.lms.execution.app;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.execution.domain.ReviewQueueEntry;
import uz.aifu.lms.execution.domain.ReviewQueueFilter;
import uz.aifu.lms.execution.persistence.ReviewQueueRepository;

/** The submissions of a course a teacher reviews; access is checked by the caller. */
@Service
public class ReviewQueueService {

    private final ReviewQueueRepository queue;

    public ReviewQueueService(ReviewQueueRepository queue) {
        this.queue = queue;
    }

    public record Page(List<ReviewQueueEntry> content, long total) {}

    @Transactional(readOnly = true)
    public Page find(UUID courseId, ReviewQueueFilter filter, String locale, int size, int page) {
        return new Page(
                queue.find(courseId, filter, locale, size, (long) page * size),
                queue.count(courseId, filter));
    }
}
