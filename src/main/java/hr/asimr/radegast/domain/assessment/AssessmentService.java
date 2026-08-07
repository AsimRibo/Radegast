package hr.asimr.radegast.domain.assessment;

import hr.asimr.radegast.data.entities.AppUser;
import hr.asimr.radegast.data.entities.Assessment;
import hr.asimr.radegast.data.entities.Course;
import hr.asimr.radegast.data.entities.Enrollment;
import hr.asimr.radegast.data.entities.enums.EnrollmentStatus;
import hr.asimr.radegast.data.entities.enums.Role;
import hr.asimr.radegast.data.repositories.AppUserRepository;
import hr.asimr.radegast.data.repositories.AssessmentRepository;
import hr.asimr.radegast.data.repositories.CourseRepository;
import hr.asimr.radegast.data.repositories.EnrollmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final CourseRepository courseRepository;
    private final AppUserRepository appUserRepository;
    private final EnrollmentRepository enrollmentRepository;

    public List<Assessment> findAssessmentsForCourse(Long courseId, String authenticatedUserEmail) {
        AppUser currentUser = findUserByEmail(authenticatedUserEmail);
        findManageableCourse(courseId, currentUser);

        return assessmentRepository.findAllByCourse_IdOrderByDueAtAsc(courseId);
    }

    public void verifyCourseManagementAccess(Long courseId, String authenticatedUserEmail) {
        AppUser currentUser = findUserByEmail(authenticatedUserEmail);
        findManageableCourse(courseId, currentUser);
    }

    public void verifyAssessmentManagementAccess(Long courseId, Long assessmentId, String authenticatedUserEmail) {
        AppUser currentUser = findUserByEmail(authenticatedUserEmail);
        findManageableCourse(courseId, currentUser);
        findAssessment(assessmentId);
    }

    @Transactional
    public void createAssessment(Long courseId, AssessmentFormDto form, String authenticatedUserEmail) {
        AppUser currentUser = findUserByEmail(authenticatedUserEmail);
        Course course = findManageableCourse(courseId, currentUser);

        Assessment assessment = new Assessment();
        assessment.setCourse(course);
        assessment.setTitle(form.getTitle().trim());
        assessment.setDescription(form.getDescription());
        assessment.setAssessmentType(form.getAssessmentType());
        assessment.setMaximumScore(form.getMaximumScore());
        assessment.setDueAt(form.getDueAt());
        assessment.setCreatedBy(currentUser);
        assessment.setCreatedAt(LocalDateTime.now());

        assessmentRepository.save(assessment);
    }

    public AssessmentFormDto findAssessmentForEdit(Long courseId, Long assessmentId, String authenticatedUserEmail) {
        AppUser currentUser = findUserByEmail(authenticatedUserEmail);
        findManageableCourse(courseId, currentUser);

        Assessment assessment = findAssessment(assessmentId);

        AssessmentFormDto form = new AssessmentFormDto();
        form.setTitle(assessment.getTitle());
        form.setDescription(assessment.getDescription());
        form.setAssessmentType(assessment.getAssessmentType());
        form.setMaximumScore(assessment.getMaximumScore());
        form.setDueAt(assessment.getDueAt());

        return form;
    }

    @Transactional
    public void updateAssessment(Long courseId, Long assessmentId, AssessmentFormDto form, String authenticatedUserEmail) {
        AppUser currentUser = findUserByEmail(authenticatedUserEmail);
        findManageableCourse(courseId, currentUser);

        Assessment assessment = findAssessment(assessmentId);

        assessment.setTitle(form.getTitle().trim());
        assessment.setDescription(form.getDescription());
        assessment.setAssessmentType(form.getAssessmentType());
        assessment.setMaximumScore(form.getMaximumScore());
        assessment.setDueAt(form.getDueAt());
    }

    public List<StudentAssessmentListDto> findAssessmentsForStudent(Long courseId, String authenticatedUserEmail) {
        AppUser student = findUserByEmail(authenticatedUserEmail);

        Enrollment enrollment = enrollmentRepository.findByStudent_IdAndCourse_Id(student.getId(), courseId)
                .orElseThrow(() -> new AccessDeniedException("Student is not enrolled in this course."));

        if (enrollment.getStatus() != EnrollmentStatus.ENROLLED) {
            throw new AccessDeniedException("Student is not enrolled in this course.");
        }

        return assessmentRepository.findAllByCourse_IdOrderByDueAtAsc(courseId)
                .stream()
                .map(assessment -> new StudentAssessmentListDto(
                        assessment.getId(),
                        assessment.getTitle(),
                        assessment.getDescription(),
                        assessment.getAssessmentType(),
                        assessment.getMaximumScore(),
                        assessment.getDueAt()
                ))
                .toList();
    }

    private AppUser findUserByEmail(String email) {
        return appUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User was not found."));
    }

    private Course findManageableCourse(Long courseId, AppUser currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course was not found"));

        if (currentUser.getRole() == Role.ADMIN) {
            return course;
        }

        if (currentUser.getRole() != Role.TEACHER) {
            throw new AccessDeniedException("User cannot manage assessments");
        }

        if (course.getTeacher() == null || !course.getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("User cannot manage assessments");
        }

        return course;
    }

    private Assessment findAssessment(Long assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment was not found."));

        return assessment;
    }
}
