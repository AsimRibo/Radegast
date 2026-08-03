package hr.asimr.radegast.services;

import hr.asimr.radegast.data.entities.AppUser;
import hr.asimr.radegast.data.entities.Course;
import hr.asimr.radegast.data.entities.enums.CourseStatus;
import hr.asimr.radegast.data.entities.enums.Role;
import hr.asimr.radegast.data.repositories.AppUserRepository;
import hr.asimr.radegast.data.repositories.CourseRepository;
import hr.asimr.radegast.domain.course.CourseFormDto;
import hr.asimr.radegast.domain.course.CourseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    private static final String TEACHER_EMAIL = "teacher@gmail.com";

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private CourseService courseService;

    @Test
    void createsDraftCourseAssignedToAuthenticatedTeacher() {
        AppUser teacher = createUser(
                10L,
                TEACHER_EMAIL,
                Role.TEACHER
        );
        CourseFormDto form = createValidFormDto();
        form.setCapacity(30);
        form.setEnrollmentOpen(true);

        when(appUserRepository.findByEmailIgnoreCase(TEACHER_EMAIL))
                .thenReturn(Optional.of(teacher));
        when(courseRepository.existsByCode("CS101"))
                .thenReturn(false);
        when(courseRepository.save(any(Course.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );
        Course result = courseService.createCourse(
                form,
                TEACHER_EMAIL
        );

        assertThat(result.getCode())
                .isEqualTo("CS101");
        assertThat(result.getName())
                .isEqualTo("Computer science");

        assertThat(result.getDescription())
                .isNull();
        assertThat(result.getCapacity())
                .isEqualTo(30);
        assertThat(result.isEnrollmentOpen())
                .isTrue();
        assertThat(result.getStatus())
                .isEqualTo(CourseStatus.DRAFT);
        assertThat(result.getTeacher())
                .isSameAs(teacher);
    }

    @Test
    void studentCannotAccessCourseManagementAndDoesNotTriggerMethods() {
        String studentEmail =
                "roki@gmail.com";
        AppUser student = createUser(
                30L,
                studentEmail,
                Role.STUDENT
        );

        when(appUserRepository.findByEmailIgnoreCase(studentEmail))
                .thenReturn(Optional.of(student));
        assertThatThrownBy(() ->
                courseService.findCoursesVisibleToAuthenticatedUser(
                        studentEmail
                )
        )
                .isInstanceOf(AccessDeniedException.class);
        verify(courseRepository, never())
                .findAllByOrderByCreatedAtDesc();
        verify(courseRepository, never())
                .findAllByTeacher_IdOrderByCreatedAtDesc(
                        any()
                );
    }

    private CourseFormDto createValidFormDto() {
        CourseFormDto form = new CourseFormDto();
        form.setCode("CS101");
        form.setName("Computer science");
        form.setEnrollmentOpen(true);
        return form;
    }

    private AppUser createUser(
            Long id,
            String email,
            Role role
    ) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setEmail(email);
        user.setFirstName("First");
        user.setLastName("Last");
        user.setRole(role);
        user.setActive(true);
        return user;
    }

    private Course createCourse(
            Long id,
            String code,
            AppUser teacher
    ) {
        Course course = new Course();
        course.setId(id);
        course.setCode(code);
        course.setName("Course " + code);
        course.setTeacher(teacher);
        course.setStatus(CourseStatus.DRAFT);
        course.setEnrollmentOpen(true);
        return course;
    }
}
