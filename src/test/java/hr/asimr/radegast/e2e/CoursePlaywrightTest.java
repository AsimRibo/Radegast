package hr.asimr.radegast.e2e;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import hr.asimr.radegast.data.entities.AppUser;
import hr.asimr.radegast.data.entities.Course;
import hr.asimr.radegast.data.entities.enums.CourseStatus;
import hr.asimr.radegast.data.entities.enums.Role;
import hr.asimr.radegast.data.repositories.AppUserRepository;
import hr.asimr.radegast.data.repositories.CourseRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@Tag("e2e")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CoursePlaywrightTest {

    @LocalServerPort
    private int serverPort;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Playwright playwright;
    private Browser browser;
    private BrowserContext browserContext;
    private Page page;

    @BeforeAll
    void startBrowser() {
        boolean headless = Boolean.parseBoolean(
                System.getProperty("playwright.headless", "true") // disable browser showing
        );

        playwright = Playwright.create();

        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(headless)
        );
    }

    @BeforeEach
    void setUp() {
        browserContext = browser.newContext();
        page = browserContext.newPage();
    }

    @AfterEach
    void closeBrowserContext() {
        if (browserContext != null) {
            browserContext.close();
        }
    }

    @AfterAll
    void closeBrowser() {
        if (browser != null) {
            browser.close();
        }

        if (playwright != null) {
            playwright.close();
        }
    }

    @Test
    void teacherCanViewOwnCourse() {
        AppUser teacher = createTeacher();
        teacher = appUserRepository.saveAndFlush(teacher);

        Course course = createCourse(teacher);
        courseRepository.saveAndFlush(course);

        page.navigate("http://localhost:" + serverPort + "/login");

        page.locator("input[type='email']").fill("teacher@gmail.com");
        page.locator("input[type='password']").fill("password");
        page.locator("button[type='submit']").click();

        assertThat(page).hasURL(Pattern.compile(".*/courses$"));
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Courses"))).isVisible();
        assertThat(page.getByText("View and manage your courses.")).isVisible();
        assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Create course"))).isVisible();
        assertThat(page.getByRole(AriaRole.COLUMNHEADER, new Page.GetByRoleOptions().setName("Teacher"))).hasCount(0);

        Locator courseRow = page.getByRole(AriaRole.ROW).filter(new Locator.FilterOptions().setHasText("JAVA"));

        assertThat(courseRow).containsText("Java Fundamentals");
        assertThat(courseRow).containsText("Introduction to Java development.");
        assertThat(courseRow).containsText("ACTIVE");
        assertThat(courseRow).containsText("25");
        assertThat(courseRow).containsText("Open");
    }

    @Test
    void courseActionsDependOnCourseStatus() {
        AppUser teacher = createSecondTeacher();
        teacher = appUserRepository.saveAndFlush(teacher);

        Course draftCourse = createDraftCourse(teacher);
        courseRepository.saveAndFlush(draftCourse);

        Course activeCourse = createCsCourse(teacher);
        courseRepository.saveAndFlush(activeCourse);

        page.navigate("http://localhost:" + serverPort + "/login");

        page.locator("input[type='email']").fill("teacher2@gmail.com");
        page.locator("input[type='password']").fill("password");
        page.locator("button[type='submit']").click();

        assertThat(page).hasURL(Pattern.compile(".*/courses$"));

        Locator draftRow = page.getByRole(AriaRole.ROW).filter(new Locator.FilterOptions().setHasText("JAVA2"));

        assertThat(draftRow.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Sessions"))).isVisible();
        assertThat(draftRow.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Edit"))).isVisible();
        assertThat(draftRow.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Activate"))).isVisible();
        assertThat(draftRow.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Archive"))).isVisible();
        assertThat(draftRow.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Return to draft"))).hasCount(0);

        Locator activeRow = page.getByRole(AriaRole.ROW).filter(new Locator.FilterOptions().setHasText("CS"));

        assertThat(activeRow.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Sessions"))).isVisible();
        assertThat(activeRow.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Edit"))).isVisible();
        assertThat(activeRow.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Return to draft"))).isVisible();
        assertThat(activeRow.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Archive"))).isVisible();
        assertThat(activeRow.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Activate"))).hasCount(0);
    }

    private static Course createDraftCourse(AppUser teacher) {
        Course draftCourse = new Course();
        draftCourse.setCode("JAVA2");
        draftCourse.setName("Java Draft Course");
        draftCourse.setTeacher(teacher);
        draftCourse.setCapacity(20);
        draftCourse.setEnrollmentOpen(true);
        draftCourse.setStatus(CourseStatus.DRAFT);
        return draftCourse;
    }

    private static Course createCourse(AppUser teacher) {
        Course course = new Course();
        course.setCode("JAVA");
        course.setName("Java Fundamentals");
        course.setDescription("Introduction to Java development.");
        course.setTeacher(teacher);
        course.setCapacity(25);
        course.setEnrollmentOpen(true);
        course.setStatus(CourseStatus.ACTIVE);
        return course;
    }

    private static Course createCsCourse(AppUser teacher) {
        Course course = new Course();
        course.setCode("CS");
        course.setName("CS Fundamentals");
        course.setDescription("Introduction to CS.");
        course.setTeacher(teacher);
        course.setCapacity(25);
        course.setEnrollmentOpen(true);
        course.setStatus(CourseStatus.ACTIVE);
        return course;
    }

    private AppUser createTeacher() {
        AppUser teacher = new AppUser();
        teacher.setEmail("teacher@gmail.com");
        teacher.setPasswordHash(passwordEncoder.encode("password"));
        teacher.setFirstName("Ivan");
        teacher.setLastName("Ivic");
        teacher.setRole(Role.TEACHER);
        teacher.setActive(true);
        return teacher;
    }

    private AppUser createSecondTeacher() {
        AppUser teacher = new AppUser();
        teacher.setEmail("teacher2@gmail.com");
        teacher.setPasswordHash(passwordEncoder.encode("password"));
        teacher.setFirstName("Marijan");
        teacher.setLastName("Ivic");
        teacher.setRole(Role.TEACHER);
        teacher.setActive(true);
        return teacher;
    }
}
