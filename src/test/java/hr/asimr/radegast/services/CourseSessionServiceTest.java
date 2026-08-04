package hr.asimr.radegast.services;

import hr.asimr.radegast.data.entities.AppUser;
import hr.asimr.radegast.data.entities.Course;
import hr.asimr.radegast.data.entities.CourseSession;
import hr.asimr.radegast.data.entities.enums.CourseStatus;
import hr.asimr.radegast.data.entities.enums.Role;
import hr.asimr.radegast.data.repositories.AppUserRepository;
import hr.asimr.radegast.data.repositories.CourseRepository;
import hr.asimr.radegast.data.repositories.CourseSessionRepository;
import hr.asimr.radegast.domain.course.CourseSessionFormDto;
import hr.asimr.radegast.domain.course.CourseSessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseSessionServiceTest {

    private static final String TEACHER_EMAIL = "teacher@gmail.com";

    private static final String OTHER_TEACHER_EMAIL = "otherteacher@gmail.com";

    private static final String ADMIN_EMAIL = "admin@gmail.com";

    @Mock
    private CourseSessionRepository courseSessionRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private CourseSessionService courseSessionService;

    @Test
    void teacherCanCreateSessionForOwnCourse() {
        AppUser teacher = new AppUser();
        teacher.setId(1L);
        teacher.setEmail(TEACHER_EMAIL);
        teacher.setRole(Role.TEACHER);
        teacher.setActive(true);

        Course course = new Course();
        course.setId(10L);
        course.setCode("CS101");
        course.setName("CS");
        course.setTeacher(teacher);
        course.setStatus(CourseStatus.ACTIVE);

        CourseSessionFormDto form = new CourseSessionFormDto();
        form.setStartsAt(LocalDateTime.of(2026, 9, 10, 9, 0));
        form.setEndsAt(LocalDateTime.of(2026, 9, 10, 11, 0));
        form.setTopic("Hot topic");
        form.setLocation("A12");

        when(appUserRepository.findByEmailIgnoreCase(TEACHER_EMAIL)).thenReturn(Optional.of(teacher));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));

        courseSessionService.createSession(10L, form, TEACHER_EMAIL);

        verify(courseSessionRepository).save(any(CourseSession.class));
    }

    @Test
    void administratorCanCreateSessionForAnyCourse() {
        AppUser administrator = new AppUser();
        administrator.setId(1L);
        administrator.setEmail(ADMIN_EMAIL);
        administrator.setRole(Role.ADMIN);
        administrator.setActive(true);

        AppUser teacher = new AppUser();
        teacher.setId(2L);
        teacher.setEmail(TEACHER_EMAIL);
        teacher.setRole(Role.TEACHER);
        teacher.setActive(true);

        Course course = new Course();
        course.setId(10L);
        course.setCode("CS101");
        course.setName("CS");
        course.setTeacher(teacher);
        course.setStatus(CourseStatus.ACTIVE);

        CourseSessionFormDto form = new CourseSessionFormDto();
        form.setStartsAt(LocalDateTime.of(2026, 9, 10, 9, 0));
        form.setEndsAt(LocalDateTime.of(2026, 9, 10, 11, 0));
        form.setTopic("Hot topic");
        form.setLocation("A12");

        when(appUserRepository.findByEmailIgnoreCase(ADMIN_EMAIL)).thenReturn(Optional.of(administrator));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));

        courseSessionService.createSession(10L, form, ADMIN_EMAIL);

        verify(courseSessionRepository).save(any(CourseSession.class));
    }

    @Test
    void teacherCannotCreateSessionForAnotherTeachersCourse() {
        AppUser currentTeacher = new AppUser();
        currentTeacher.setId(1L);
        currentTeacher.setEmail(TEACHER_EMAIL);
        currentTeacher.setRole(Role.TEACHER);
        currentTeacher.setActive(true);

        AppUser assignedTeacher = new AppUser();
        assignedTeacher.setId(2L);
        assignedTeacher.setEmail(OTHER_TEACHER_EMAIL);
        assignedTeacher.setRole(Role.TEACHER);
        assignedTeacher.setActive(true);

        Course course = new Course();
        course.setId(10L);
        course.setCode("CS101");
        course.setName("CS");
        course.setTeacher(assignedTeacher);
        course.setStatus(CourseStatus.ACTIVE);

        CourseSessionFormDto form = new CourseSessionFormDto();
        form.setStartsAt(LocalDateTime.of(2026, 9, 10, 9, 0));
        form.setEndsAt(LocalDateTime.of(2026, 9, 10, 11, 0));
        form.setTopic("Hot topic");
        form.setLocation("A12");

        when(appUserRepository.findByEmailIgnoreCase(TEACHER_EMAIL)).thenReturn(Optional.of(currentTeacher));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() ->
                courseSessionService.createSession(10L, form, TEACHER_EMAIL)
        )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You cannot manage sessions for this course.");

        verify(courseSessionRepository, never()).save(any());
    }
}
