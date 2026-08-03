package hr.asimr.radegast.integration;

import hr.asimr.radegast.config.SecurityConfig;
import hr.asimr.radegast.controllers.course.CourseController;
import hr.asimr.radegast.domain.course.CourseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
@Import(SecurityConfig.class)
public class CourseControllerTest {

    private static final String TEACHER_EMAIL = "teacher@gmail.com";
    private static final String ADMIN_EMAIL = "admin@gmail.com";
    private static final String STUDENT_EMAIL = "student@gmail.com";
    private static final String PASSWORD = "password";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CourseService courseService;

    @Test
    void studentCannotOpenCourseCreationForm() throws Exception {
        mockMvc.perform(
                        get("/courses/new")
                                .with(user(STUDENT_EMAIL)
                                        .roles("STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void teacherCanOpenCourseCreationForm() throws Exception {
        mockMvc.perform(
                        get("/courses/new")
                                .with(user(TEACHER_EMAIL)
                                        .roles("TEACHER")))
                .andExpect(status().isOk())
                .andExpect(view().name("courses/form"))
                .andExpect(model().attributeExists("courseForm"))
                .andExpect(model().attribute(
                        "formTitle",
                        "Create course"
                ))
                .andExpect(model().attribute(
                        "formAction",
                        "/courses"
                ))
                .andExpect(model().attribute(
                        "submitLabel",
                        "Create course"
                ));
    }

    @Test
    void administratorCanViewAllCourses() throws Exception {
        when(courseService.findCoursesVisibleToAuthenticatedUser(ADMIN_EMAIL))
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/courses")
                                .with(user(ADMIN_EMAIL)
                                        .roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("courses/list"))
                .andExpect(model().attribute("courses", List.of()))
                .andExpect(model().attribute("canCreateCourse", false))
                .andExpect(model().attribute("showTeacherColumn", true));

        verify(courseService).findCoursesVisibleToAuthenticatedUser(ADMIN_EMAIL);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestSecurityConfiguration {

        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        UserDetailsService userDetailsService(
                PasswordEncoder passwordEncoder
        ) {
            return new InMemoryUserDetailsManager(
                    User.withUsername(ADMIN_EMAIL)
                            .password(passwordEncoder.encode(PASSWORD))
                            .roles("ADMIN")
                            .build(),

                    User.withUsername(TEACHER_EMAIL)
                            .password(passwordEncoder.encode(PASSWORD))
                            .roles("TEACHER")
                            .build(),

                    User.withUsername(STUDENT_EMAIL)
                            .password(passwordEncoder.encode(PASSWORD))
                            .roles("STUDENT")
                            .build()
            );
        }
    }
}
