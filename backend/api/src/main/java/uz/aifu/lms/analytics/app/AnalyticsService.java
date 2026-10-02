package uz.aifu.lms.analytics.app;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.aifu.lms.analytics.domain.CodingTaskDifficultyView;
import uz.aifu.lms.analytics.domain.CourseCompletionView;
import uz.aifu.lms.analytics.domain.LessonCompletionView;
import uz.aifu.lms.analytics.domain.QuestionDifficultyView;
import uz.aifu.lms.analytics.domain.QuizPerformanceView;
import uz.aifu.lms.analytics.domain.StudentAtRiskView;
import uz.aifu.lms.analytics.persistence.CodingTaskDifficultyViewRepository;
import uz.aifu.lms.analytics.persistence.CourseCompletionViewRepository;
import uz.aifu.lms.analytics.persistence.LessonCompletionViewRepository;
import uz.aifu.lms.analytics.persistence.QuestionDifficultyViewRepository;
import uz.aifu.lms.analytics.persistence.QuizPerformanceViewRepository;
import uz.aifu.lms.analytics.persistence.StudentAtRiskViewRepository;

/**
 * Read-only facade over V015's analytics views (brief §26). No write path exists or is planned —
 * every method here is a query against a Postgres view, never a table Java owns.
 */
@Service
public class AnalyticsService {

    private final CourseCompletionViewRepository courseCompletion;
    private final LessonCompletionViewRepository lessonCompletion;
    private final QuizPerformanceViewRepository quizPerformance;
    private final QuestionDifficultyViewRepository questionDifficulty;
    private final CodingTaskDifficultyViewRepository codingTaskDifficulty;
    private final StudentAtRiskViewRepository studentAtRisk;

    public AnalyticsService(
            CourseCompletionViewRepository courseCompletion,
            LessonCompletionViewRepository lessonCompletion,
            QuizPerformanceViewRepository quizPerformance,
            QuestionDifficultyViewRepository questionDifficulty,
            CodingTaskDifficultyViewRepository codingTaskDifficulty,
            StudentAtRiskViewRepository studentAtRisk) {
        this.courseCompletion = courseCompletion;
        this.lessonCompletion = lessonCompletion;
        this.quizPerformance = quizPerformance;
        this.questionDifficulty = questionDifficulty;
        this.codingTaskDifficulty = codingTaskDifficulty;
        this.studentAtRisk = studentAtRisk;
    }

    @Transactional(readOnly = true)
    public Optional<CourseCompletionView> courseCompletion(UUID courseId) {
        return courseCompletion.findByCourseId(courseId);
    }

    @Transactional(readOnly = true)
    public List<LessonCompletionView> lessonCompletion(UUID courseId) {
        return lessonCompletion.findByCourseIdOrderByLessonId(courseId);
    }

    @Transactional(readOnly = true)
    public List<QuizPerformanceView> quizPerformanceForCourse(UUID courseId) {
        return quizPerformance.findByCourseIdOrderByQuizId(courseId);
    }

    @Transactional(readOnly = true)
    public Optional<QuizPerformanceView> quizPerformance(UUID quizId) {
        return quizPerformance.findByQuizId(quizId);
    }

    @Transactional(readOnly = true)
    public List<QuestionDifficultyView> questionDifficulty(UUID quizId) {
        return questionDifficulty.findByQuizIdOrderByQuestionId(quizId);
    }

    @Transactional(readOnly = true)
    public List<CodingTaskDifficultyView> codingTaskDifficultyForCourse(UUID courseId) {
        return codingTaskDifficulty.findByCourseIdOrderByCodingTaskId(courseId);
    }

    @Transactional(readOnly = true)
    public Optional<CodingTaskDifficultyView> codingTaskDifficulty(UUID codingTaskId) {
        return codingTaskDifficulty.findByCodingTaskId(codingTaskId);
    }

    @Transactional(readOnly = true)
    public Page<StudentAtRiskView> studentsAtRisk(UUID courseId, Pageable pageable) {
        return studentAtRisk.findByCourseId(courseId, pageable);
    }
}
