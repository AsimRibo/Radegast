package hr.asimr.radegast.domain.admin;

import hr.asimr.radegast.data.entities.AppUser;
import hr.asimr.radegast.data.entities.Course;
import hr.asimr.radegast.data.entities.Enrollment;
import hr.asimr.radegast.data.entities.enums.CourseStatus;
import hr.asimr.radegast.data.entities.enums.EnrollmentStatus;
import hr.asimr.radegast.data.entities.enums.Role;
import hr.asimr.radegast.data.repositories.AppUserRepository;
import hr.asimr.radegast.data.repositories.CourseRepository;
import hr.asimr.radegast.data.repositories.EnrollmentRepository;
import hr.asimr.radegast.domain.course.EnrollmentOperationException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseEnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final AppUserRepository appUserRepository;

    public CourseEnrollmentListDto findCourseEnrollments(Long courseId, String authenticatedUserEmail) {
        AppUser currentUser = findAuthenticatedUser(authenticatedUserEmail);
        Course course = findViewableCourse(courseId, currentUser);

        List<Enrollment> courseEnrollments =
                enrollmentRepository.findAllByCourse_IdOrderByStudent_LastNameAscStudent_FirstNameAsc(courseId);

        List<CourseEnrolledStudentDto> enrolledStudents = courseEnrollments.stream()
                .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.ENROLLED)
                .map(this::createEnrolledStudentDto)
                .toList();

        boolean administrator = currentUser.getRole() == Role.ADMIN;

        List<CourseEnrollmentCandidateDto> availableStudents = administrator
                ? findAvailableStudents(courseEnrollments)
                : List.of();

        boolean acceptingEnrollments = isAcceptingEnrollments(course, enrolledStudents.size());

        return new CourseEnrollmentListDto(
                course.getId(),
                course.getCode(),
                course.getName(),
                getTeacherName(course),
                course.getCapacity(),
                enrolledStudents.size(),
                administrator,
                acceptingEnrollments,
                enrolledStudents,
                availableStudents
        );
    }

    @Transactional
    public void enrollStudent(Long courseId, Long studentId, String authenticatedUserEmail) {
        AppUser administrator = findAdministrator(authenticatedUserEmail);
        Course course = findCourse(courseId);
        AppUser student = findActiveStudent(studentId);

        Enrollment existingEnrollment = enrollmentRepository
                .findByStudent_IdAndCourse_Id(studentId, courseId)
                .orElse(null);

        if (existingEnrollment != null && existingEnrollment.getStatus() == EnrollmentStatus.ENROLLED) {
            throw new EnrollmentOperationException("The student is already enrolled in this course.");
        }

        if (existingEnrollment != null && existingEnrollment.getStatus() == EnrollmentStatus.COMPLETED) {
            throw new EnrollmentOperationException("A completed enrolment cannot be reopened.");
        }

        ensureCourseAcceptsEnrollment(course);
        ensureCourseHasCapacity(course);

        if (existingEnrollment != null) {
            reactivateEnrollment(existingEnrollment, administrator);
            return;
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setCompletedAt(null);
        enrollment.setEnrolledBy(administrator);

        enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void withdrawStudent(Long courseId, Long studentId, String authenticatedUserEmail) {
        findAdministrator(authenticatedUserEmail);

        Enrollment enrollment = enrollmentRepository
                .findByStudent_IdAndCourse_Id(studentId, courseId)
                .orElseThrow(() -> new EnrollmentOperationException("The student is not enrolled in this course"));

        enrollment.setStatus(EnrollmentStatus.WITHDRAWN);
        enrollment.setCompletedAt(null);

        enrollmentRepository.save(enrollment);
    }

    private CourseEnrolledStudentDto createEnrolledStudentDto(Enrollment enrollment) {
        AppUser student = enrollment.getStudent();

        return new CourseEnrolledStudentDto(
                student.getId(),
                getUserName(student),
                student.getEmail(),
                enrollment.getEnrolledAt()
        );
    }

    private List<CourseEnrollmentCandidateDto> findAvailableStudents(List<Enrollment> courseEnrollments) {
        Set<Long> unavailableStudentIds = courseEnrollments.stream()
                .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.ENROLLED
                                      || enrollment.getStatus() == EnrollmentStatus.COMPLETED)
                .map(enrollment -> enrollment.getStudent().getId())
                .collect(Collectors.toSet());

        return appUserRepository.findAllByRoleAndActiveTrueOrderByLastNameAscFirstNameAsc(Role.STUDENT)
                .stream()
                .filter(student -> !unavailableStudentIds.contains(student.getId()))
                .map(student -> new CourseEnrollmentCandidateDto(
                        student.getId(),
                        getUserName(student),
                        student.getEmail()
                ))
                .toList();
    }

    private void reactivateEnrollment(Enrollment enrollment, AppUser administrator) {
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setCompletedAt(null);
        enrollment.setEnrolledBy(administrator);

        enrollmentRepository.save(enrollment);
    }

    private Course findViewableCourse(Long courseId, AppUser currentUser) {
        Course course = findCourse(courseId);

        if (currentUser.getRole() == Role.ADMIN) {
            return course;
        }

        if (currentUser.getRole() == Role.TEACHER
            && course.getTeacher() != null
            && course.getTeacher().getId().equals(currentUser.getId())) {
            return course;
        }

        throw new AccessDeniedException("You cannot view enrolments for this course.");
    }

    private AppUser findAdministrator(String authenticatedUserEmail) {
        AppUser currentUser = findAuthenticatedUser(authenticatedUserEmail);

        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Something went wrong.");
        }

        return currentUser;
    }

    private AppUser findActiveStudent(Long studentId) {
        AppUser student = appUserRepository.findById(studentId)
                .orElseThrow(() -> new EnrollmentOperationException("Student was not found."));

        if (!student.isActive()) {
            throw new EnrollmentOperationException("Inactive students cannot be enrolled in courses.");
        }

        return student;
    }

    private AppUser findAuthenticatedUser(String authenticatedUserEmail) {
        return appUserRepository.findByEmailIgnoreCase(authenticatedUserEmail)
                .orElseThrow(() -> new EnrollmentOperationException("Authenticated user was not found."));
    }

    private Course findCourse(Long courseId) {
        return courseRepository
                .findById(courseId)
                .orElseThrow(() -> new EnrollmentOperationException("Course was not found."));
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

    private boolean isAcceptingEnrollments(Course course, long enrolledCount) {
        if (course.getStatus() != CourseStatus.ACTIVE || !course.isEnrollmentOpen()) {
            return false;
        }

        return course.getCapacity() == null || enrolledCount < course.getCapacity();
    }

    private String getUserName(AppUser user) {
        return user.getFirstName().trim() + " " + user.getLastName().trim();
    }

    private String getTeacherName(Course course) {
        if (course.getTeacher() == null) {
            return "Not assigned";
        }

        return getUserName(course.getTeacher());
    }
}
