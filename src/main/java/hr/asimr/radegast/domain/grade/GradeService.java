package hr.asimr.radegast.domain.grade;

import hr.asimr.radegast.data.entities.*;
import hr.asimr.radegast.data.entities.enums.EnrollmentStatus;
import hr.asimr.radegast.data.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRepository gradeRepository;
    private final AssessmentRepository assessmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final AppUserRepository appUserRepository;

    public Assessment findAssessmentForTeacher(Long assessmentId, String authenticatedUserEmail) {
        AppUser teacher = findUser(authenticatedUserEmail);

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found."));

        if (assessment.getCourse().getTeacher() == null
            || !Objects.equals(assessment.getCourse().getTeacher().getId(), teacher.getId())) {
            throw new AccessDeniedException("You cannot grade this assessment.");
        }

        return assessment;
    }

    public List<TeacherGradeRowDto> findStudentsForGrading(Assessment assessment) {
        List<Enrollment> enrollments = enrollmentRepository.findAllByCourse_IdAndStatus(
                assessment.getCourse().getId(),
                EnrollmentStatus.ENROLLED
        );

        return enrollments.stream()
                .map(enrollment -> {
                    AppUser student = enrollment.getStudent();

                    Grade grade = gradeRepository.findByAssessment_IdAndStudent_Id(
                            assessment.getId(),
                            student.getId()
                    ).orElse(null);

                    return new TeacherGradeRowDto(
                            student.getId(),
                            student.getFirstName() + " " + student.getLastName(),
                            grade != null ? grade.getScore() : null,
                            grade != null ? grade.getFeedback() : null
                    );
                })
                .toList();
    }

    @Transactional
    public void saveGrade(
            Long assessmentId,
            GradeFormDto form,
            String authenticatedUserEmail
    ) {
        AppUser teacher = findUser(authenticatedUserEmail);

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found."));

        if (assessment.getCourse().getTeacher() == null
            || !Objects.equals(assessment.getCourse().getTeacher().getId(), teacher.getId())) {
            throw new AccessDeniedException("You cannot grade this assessment.");
        }

        if (form.getScore() == null || form.getScore() < 0 || form.getScore() > assessment.getMaximumScore()) {
            throw new IllegalArgumentException("Check score");
        }

        Enrollment enrollment = enrollmentRepository
                .findByStudent_IdAndCourse_Id(
                        form.getStudentId(),
                        assessment.getCourse().getId()
                )
                .orElseThrow(() -> new IllegalArgumentException("Student is not enrolled in this course."));

        if (enrollment.getStatus() != EnrollmentStatus.ENROLLED) {
            throw new IllegalArgumentException("Student is not currently enrolled in this course.");
        }

        Grade grade = gradeRepository.findByAssessment_IdAndStudent_Id(assessment.getId(), enrollment.getStudent().getId())
                .orElseGet(Grade::new);

        grade.setAssessment(assessment);
        grade.setStudent(enrollment.getStudent());
        grade.setScore(form.getScore());
        grade.setFeedback(form.getFeedback().trim());
        grade.setGradedBy(teacher);
        grade.setGradedAt(LocalDateTime.now());

        gradeRepository.save(grade);
    }

    public Course findCourseForStudent(Long courseId, String authenticatedUserEmail) {
        AppUser student = findUser(authenticatedUserEmail);

        Course course = courseRepository.findById(courseId).orElseThrow(() -> new IllegalArgumentException("Course not found."));

        enrollmentRepository.findByStudent_IdAndCourse_Id(student.getId(), courseId)
                .orElseThrow(() -> new AccessDeniedException("You cannot view grades for this course."));

        return course;
    }

    public List<StudentGradeDto> findGradesForStudent(
            Course course,
            String authenticatedUserEmail
    ) {
        AppUser student = findUser(authenticatedUserEmail);

        List<Assessment> assessments = assessmentRepository.findAllByCourse_IdOrderByDueAtAsc(course.getId());

        return assessments.stream()
                .map(assessment -> {
                    Grade grade = gradeRepository.findByAssessment_IdAndStudent_Id(
                            assessment.getId(),
                            student.getId()
                    ).orElse(null);

                    return new StudentGradeDto(
                            assessment.getId(),
                            assessment.getTitle(),
                            assessment.getAssessmentType(),
                            assessment.getMaximumScore(),
                            assessment.getDueAt(),
                            grade != null ? grade.getScore() : null,
                            grade != null ? grade.getFeedback() : null
                    );
                })
                .toList();
    }

    public StudentProgressDto calculateProgress(List<StudentGradeDto> grades) {
        int earnedScore = grades.stream()
                .filter(grade -> grade.getScore() != null)
                .mapToInt(StudentGradeDto::getScore)
                .sum();

        int maximumScore = grades.stream()
                .filter(grade -> grade.getScore() != null)
                .mapToInt(StudentGradeDto::getMaximumScore)
                .sum();

        if (maximumScore == 0) {
            return new StudentProgressDto(
                    0,
                    0,
                    0,
                    false
            );
        }

        int percentage = (int) Math.round((earnedScore * 100.0) / maximumScore);

        return new StudentProgressDto(
                earnedScore,
                maximumScore,
                percentage,
                true
        );
    }

    private AppUser findUser(String email) {
        return appUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }
}
