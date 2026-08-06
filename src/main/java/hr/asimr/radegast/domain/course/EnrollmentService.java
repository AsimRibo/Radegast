package hr.asimr.radegast.domain.course;

import hr.asimr.radegast.data.entities.AppUser;
import hr.asimr.radegast.data.entities.Course;
import hr.asimr.radegast.data.entities.CourseSession;
import hr.asimr.radegast.data.entities.Enrollment;
import hr.asimr.radegast.data.entities.enums.CourseStatus;
import hr.asimr.radegast.data.entities.enums.EnrollmentStatus;
import hr.asimr.radegast.data.entities.enums.Role;
import hr.asimr.radegast.data.repositories.AppUserRepository;
import hr.asimr.radegast.data.repositories.CourseRepository;
import hr.asimr.radegast.data.repositories.CourseSessionRepository;
import hr.asimr.radegast.data.repositories.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final CourseSessionRepository courseSessionRepository;
    private final AppUserRepository appUserRepository;

    public List<StudentCourseListDto> findCoursesForStudent(String authenticatedUserEmail) {
        AppUser student = findActiveStudent(authenticatedUserEmail);

        Map<Long, Enrollment> enrollmentByCourseId = enrollmentRepository.findAllByStudent_Id(student.getId())
                .stream()
                .collect(Collectors.toMap(
                        enrollment -> enrollment.getCourse().getId(),
                        enrollment -> enrollment
                ));

        return courseRepository.findAllByStatusOrderByName(CourseStatus.ACTIVE)
                .stream()
                .map(course -> createStudentCourseDto(course, enrollmentByCourseId.get(course.getId())))
                .toList();
    }

    @Transactional
    public void enroll(Long courseId, String authenticatedUserEmail) {
        AppUser student = findActiveStudent(authenticatedUserEmail);
        Course course = findCourse(courseId);

        Enrollment existingEnrollment = enrollmentRepository.findByStudent_IdAndCourse_Id(student.getId(), courseId).orElse(null);

        if (existingEnrollment != null && existingEnrollment.getStatus() == EnrollmentStatus.ENROLLED) {
            throw new EnrollmentOperationException("You are already enrolled in this course");
        }

        if (existingEnrollment != null && existingEnrollment.getStatus() == EnrollmentStatus.COMPLETED) {
            throw new EnrollmentOperationException("A completed enrolment cannot be reopened");
        }

        ensureCourseAcceptsEnrollment(course);
        ensureCourseHasCapacity(course);

        if (existingEnrollment != null) {
            reactivateEnrollment(existingEnrollment, student);
            return;
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setCompletedAt(null);
        enrollment.setEnrolledBy(student);

        enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void withdraw(Long courseId, String authenticatedUserEmail) {
        AppUser student = findActiveStudent(authenticatedUserEmail);

        Enrollment enrollment = enrollmentRepository
                .findByStudent_IdAndCourse_Id(student.getId(), courseId)
                .orElseThrow(() -> new EnrollmentOperationException("You are not enrolled in this course."));

        if (enrollment.getStatus() != EnrollmentStatus.ENROLLED) {
            throw new EnrollmentOperationException("Only an active enrolment can be withdrawn.");
        }

        enrollment.setStatus(EnrollmentStatus.WITHDRAWN);
        enrollment.setCompletedAt(null);

        enrollmentRepository.save(enrollment);
    }

    public StudentCourseDetailsDto findEnrolledCourseDetails(Long courseId, String authenticatedUserEmail) {
        AppUser student = findActiveStudent(authenticatedUserEmail);

        Enrollment enrollment = enrollmentRepository
                .findByStudent_IdAndCourse_Id(student.getId(), courseId)
                .orElseThrow(() -> new EnrollmentOperationException("You are not enrolled in this course."));

        if (enrollment.getStatus() != EnrollmentStatus.ENROLLED) {
            throw new EnrollmentOperationException("You must be enrolled in the course to view its sessions.");
        }

        Course course = findCourse(courseId);

        List<CourseSession> sessions = courseSessionRepository.findAllByCourse_IdOrderByStartsAtAsc(courseId);

        return new StudentCourseDetailsDto(
                course.getId(),
                course.getCode(),
                course.getName(),
                course.getDescription(),
                getTeacherName(course),
                sessions
        );
    }

    private StudentCourseListDto createStudentCourseDto(Course course, Enrollment enrollment) {
        long enrolledCount = enrollmentRepository.countByCourse_IdAndStatus(course.getId(), EnrollmentStatus.ENROLLED);

        boolean full = course.getCapacity() != null && enrolledCount >= course.getCapacity();

        EnrollmentStatus enrollmentStatus = enrollment == null ? null : enrollment.getStatus();

        return new StudentCourseListDto(
                course.getId(),
                course.getCode(),
                course.getName(),
                course.getDescription(),
                getTeacherName(course),
                course.getCapacity(),
                enrolledCount,
                course.isEnrollmentOpen(),
                enrollmentStatus,
                full
        );
    }

    private void reactivateEnrollment(Enrollment enrollment, AppUser student) {
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setCompletedAt(null);
        enrollment.setEnrolledBy(student);

        enrollmentRepository.save(enrollment);
    }

    private void ensureCourseAcceptsEnrollment(Course course) {
        if (course.getStatus() != CourseStatus.ACTIVE) {
            throw new EnrollmentOperationException("Only active courses accept enrolments.");
        }

        if (!course.isEnrollmentOpen()) {
            throw new EnrollmentOperationException("Enrolment for this course is closed.");
        }
    }

    private void ensureCourseHasCapacity(Course course) {
        if (course.getCapacity() == null) {
            return;
        }

        long enrolledCount = enrollmentRepository.countByCourse_IdAndStatus(course.getId(), EnrollmentStatus.ENROLLED);

        if (enrolledCount >= course.getCapacity()) {
            throw new EnrollmentOperationException("The course has reached its capacity.");
        }
    }

    private Course findCourse(Long courseId) {
        return courseRepository.findById(courseId).orElseThrow(() -> new EnrollmentOperationException("Course was not found."));
    }

    private AppUser findActiveStudent(String authenticatedUserEmail) {
        AppUser student = appUserRepository.findByEmailIgnoreCase(authenticatedUserEmail)
                .orElseThrow(() -> new EnrollmentOperationException("Authenticated user was not found."));

        if (student.getRole() != Role.STUDENT) {
            throw new EnrollmentOperationException("Only students can enrol in courses.");
        }

        if (!student.isActive()) {
            throw new EnrollmentOperationException("Inactive students cannot enrol in courses.");
        }

        return student;
    }

    private String getTeacherName(Course course) {
        if (course.getTeacher() == null) {
            return "Not assigned";
        }

        return course.getTeacher().getFirstName().trim() + " " + course.getTeacher().getLastName().trim();
    }
}
