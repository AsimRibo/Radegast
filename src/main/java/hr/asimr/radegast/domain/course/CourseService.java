package hr.asimr.radegast.domain.course;

import hr.asimr.radegast.data.entities.AppUser;
import hr.asimr.radegast.data.entities.Course;
import hr.asimr.radegast.data.entities.enums.CourseStatus;
import hr.asimr.radegast.data.entities.enums.Role;
import hr.asimr.radegast.data.repositories.AppUserRepository;
import hr.asimr.radegast.data.repositories.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final AppUserRepository appUserRepository;

    public List<CourseListDto> findCoursesVisibleToAuthenticatedUser(String authenticatedUserEmail) {
        AppUser currentUser = findUserByEmail(authenticatedUserEmail);

        List<Course> courses = switch (currentUser.getRole()) {
            case ADMIN -> courseRepository.findAllByOrderByCreatedAtDesc();

            case TEACHER -> courseRepository.findAllByTeacher_IdOrderByCreatedAtDesc(currentUser.getId());

            case STUDENT -> throw new AccessDeniedException(
                    "Students cannot access course management."
            );
        };

        return courses
                .stream()
                .map(CourseListDto::from)
                .toList();
    }

    @Transactional
    public Course createCourse(CourseFormDto courseForm, String authenticatedUserEmail) {
        AppUser teacher = findUserByEmail(authenticatedUserEmail);

        if (teacher.getRole() != Role.TEACHER) {
            throw new AccessDeniedException("Only teachers can create courses.");
        }

        String normalizedCode = normalizeCode(courseForm.getCode());

        if (courseRepository.existsByCode(normalizedCode)) {
            throw new CourseCodeAlreadyExistsException(normalizedCode);
        }

        Course course = new Course();
        course.setCode(normalizedCode);
        course.setName(courseForm.getName().trim());
        course.setDescription(normalizeDescription(courseForm.getDescription()));
        course.setCapacity(courseForm.getCapacity());
        course.setEnrollmentOpen(courseForm.isEnrollmentOpen());
        course.setTeacher(teacher);
        course.setStatus(CourseStatus.DRAFT);

        return courseRepository.save(course);
    }

    private AppUser findUserByEmail(String email) {
        return appUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User with email '" + email + "' was not found.")
                );
    }

    private String normalizeCode(String code) {
        return code
                .trim()
                .toUpperCase();
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }
}
