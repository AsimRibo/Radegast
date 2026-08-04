package hr.asimr.radegast.domain.course;

import hr.asimr.radegast.data.entities.AppUser;
import hr.asimr.radegast.data.entities.Course;
import hr.asimr.radegast.data.entities.CourseSession;
import hr.asimr.radegast.data.entities.enums.CourseStatus;
import hr.asimr.radegast.data.entities.enums.Role;
import hr.asimr.radegast.data.repositories.AppUserRepository;
import hr.asimr.radegast.data.repositories.CourseRepository;
import hr.asimr.radegast.data.repositories.CourseSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseSessionService {

    private final CourseSessionRepository courseSessionRepository;
    private final CourseRepository courseRepository;
    private final AppUserRepository appUserRepository;

    public CourseSessionsPageDto findSessionsForCourse(Long courseId, String authenticatedUserEmail) {
        AppUser currentUser = findUserByEmail(authenticatedUserEmail);

        Course course = findViewableCourse(courseId, currentUser);

        List<CourseSession> sessions;

        if (currentUser.getRole() == Role.STUDENT) {
            sessions = courseSessionRepository
                    .findAllByCourse_IdAndStartsAtGreaterThanEqualOrderByStartsAtAsc(courseId, LocalDateTime.now());
        } else {
            sessions = courseSessionRepository.findAllByCourse_IdOrderByStartsAtAsc(courseId);
        }

        return new CourseSessionsPageDto(
                CourseSessionCourseDto.from(course),
                sessions.stream()
                        .map(CourseSessionListDto::from)
                        .toList(),
                canManageCourse(course, currentUser)
        );
    }

    public CourseSessionCourseDto getCourseForSessionManagement(Long courseId, String authenticatedUserEmail) {
        AppUser currentUser = findUserByEmail(authenticatedUserEmail);

        Course course = findManageableCourse(courseId, currentUser);

        ensureCourseAllowsSessionChanges(course);

        return CourseSessionCourseDto.from(course);
    }

    public CourseSessionFormDto getSessionForEditing(Long courseId, Long sessionId, String authenticatedUserEmail) {
        AppUser currentUser = findUserByEmail(authenticatedUserEmail);

        Course course = findManageableCourse(courseId, currentUser);

        ensureCourseAllowsSessionChanges(course);

        CourseSession courseSession = findSession(sessionId, courseId);

        ensureSessionCanBeChanged(courseSession);

        return CourseSessionFormDto.from(courseSession);
    }

    @Transactional
    public void createSession(Long courseId, CourseSessionFormDto sessionForm, String authenticatedUserEmail) {
        AppUser currentUser = findUserByEmail(authenticatedUserEmail);

        Course course = findManageableCourse(courseId, currentUser);

        ensureCourseAllowsSessionChanges(course);
        validateTimeRange(sessionForm);

        CourseSession courseSession = new CourseSession();
        courseSession.setCourse(course);
        courseSession.setStartsAt(sessionForm.getStartsAt());
        courseSession.setEndsAt(sessionForm.getEndsAt());
        courseSession.setTopic(normalizeOptionalText(sessionForm.getTopic()));
        courseSession.setLocation(normalizeOptionalText(sessionForm.getLocation()));
        courseSession.setCancelled(false);

        courseSessionRepository.save(courseSession);
    }

    @Transactional
    public void updateSession(
            Long courseId,
            Long sessionId,
            CourseSessionFormDto sessionForm,
            String authenticatedUserEmail
    ) {
        AppUser currentUser = findUserByEmail(authenticatedUserEmail);

        Course course = findManageableCourse(courseId, currentUser);

        ensureCourseAllowsSessionChanges(course);
        validateTimeRange(sessionForm);

        CourseSession courseSession = findSession(sessionId, courseId);

        ensureSessionCanBeChanged(courseSession);

        courseSession.setStartsAt(sessionForm.getStartsAt());
        courseSession.setEndsAt(sessionForm.getEndsAt());
        courseSession.setTopic(normalizeOptionalText(sessionForm.getTopic()));
        courseSession.setLocation(normalizeOptionalText(sessionForm.getLocation()));
    }

    @Transactional
    public void cancelSession(Long courseId, Long sessionId, String authenticatedUserEmail) {
        AppUser currentUser = findUserByEmail(authenticatedUserEmail);

        Course course = findManageableCourse(courseId, currentUser);

        ensureCourseAllowsSessionChanges(course);

        CourseSession courseSession = findSession(sessionId, courseId);

        ensureSessionCanBeChanged(courseSession);

        courseSession.setCancelled(true);
    }

    private Course findViewableCourse(Long courseId, AppUser currentUser) {
        Course course = findCourse(courseId);

        if (currentUser.getRole() == Role.STUDENT) {
            if (course.getStatus() != CourseStatus.ACTIVE) {
                throw new AccessDeniedException("Students can view sessions only for active courses.");
            }

            return course;
        }

        if (!canManageCourse(course, currentUser)) {
            throw new AccessDeniedException("You cannot view sessions for this course.");
        }

        return course;
    }

    private Course findManageableCourse(Long courseId, AppUser currentUser) {
        Course course = findCourse(courseId);

        if (!canManageCourse(course, currentUser)) {
            throw new AccessDeniedException("You cannot manage sessions for this course.");
        }

        return course;
    }

    private boolean canManageCourse(
            Course course,
            AppUser currentUser
    ) {
        if (currentUser.getRole() == Role.ADMIN) {
            return true;
        }

        return currentUser.getRole() == Role.TEACHER
               && course.getTeacher() != null
               && course.getTeacher()
                       .getId()
                       .equals(currentUser.getId());
    }

    private Course findCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
    }

    private CourseSession findSession(Long sessionId, Long courseId) {
        return courseSessionRepository
                .findByIdAndCourse_Id(sessionId, courseId)
                .orElseThrow(() -> new CourseSessionNotFoundException(sessionId, courseId));
    }

    private AppUser findUserByEmail(String email) {
        return appUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with email '" + email + "' was not found."));
    }

    private void validateTimeRange(CourseSessionFormDto sessionForm) {
        LocalDateTime startsAt = sessionForm.getStartsAt();

        LocalDateTime endsAt = sessionForm.getEndsAt();

        if (startsAt == null || endsAt == null || !endsAt.isAfter(startsAt)) {
            throw new IllegalArgumentException("End time must be later than start time.");
        }
    }

    private void ensureCourseAllowsSessionChanges(Course course) {
        if (course.getStatus() == CourseStatus.ARCHIVED) {
            throw new CourseSessionOperationNotAllowedException("Sessions belonging to an archived course cannot be changed.");
        }
    }

    private void ensureSessionCanBeChanged(CourseSession courseSession) {
        if (courseSession.isCancelled()) {
            throw new CourseSessionOperationNotAllowedException("A cancelled session cannot be changed.");
        }
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        if (normalized.isEmpty()) {
            return null;
        }

        return normalized;
    }
}
