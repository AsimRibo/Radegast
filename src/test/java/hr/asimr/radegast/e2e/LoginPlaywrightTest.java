package hr.asimr.radegast.e2e;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import hr.asimr.radegast.data.entities.AppUser;
import hr.asimr.radegast.data.entities.enums.Role;
import hr.asimr.radegast.data.repositories.AppUserRepository;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@Tag("e2e")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LoginPlaywrightTest {

    private static final String TEST_EMAIL = "user@gmail.com";
    private static final String TEST_PASSWORD = "password";

    @LocalServerPort
    private int serverPort;

    @Autowired
    private AppUserRepository appUserRepository;

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
    void activeStudentCanLogIn() throws IOException {
        createStudentTestUser();

        page.navigate(applicationUrl("/login"));

        page.locator("input[type='email']").fill(TEST_EMAIL);
        page.locator("input[type='password']").fill(TEST_PASSWORD);
        page.locator("button[type='submit']").click();

        assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Log out"))).isVisible();

        saveStudentScreenshot();
    }

    @Test
    void activeTeacherCanLogIn() throws IOException {
        createTeacherTestUser();

        page.navigate(applicationUrl("/login"));

        page.locator("input[type='email']").fill(TEST_EMAIL);
        page.locator("input[type='password']").fill(TEST_PASSWORD);
        page.locator("button[type='submit']").click();

        assertThat(page).hasURL(Pattern.compile(".*/courses"));
        assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Log out"))).isVisible();

        saveTeacherScreenshot();
    }

    @Test
    void invalidCredentialsDoNotLogInUser() throws IOException {
        page.navigate(applicationUrl("/login"));

        page.locator("input[type='email']").fill(TEST_EMAIL);
        page.locator("input[type='password']").fill("wrong-password");
        page.locator("button[type='submit']").click();

        assertThat(page).hasURL(Pattern.compile(".*/login\\?error$"));
        assertThat(page.getByRole(AriaRole.ALERT)).hasText("Invalid credentials. Please try again.");
        assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign in"))).isVisible();

        saveStudentFailScreenshot();
    }

    private void createStudentTestUser() {
        AppUser user = appUserRepository
                .findByEmailIgnoreCase(TEST_EMAIL)
                .orElseGet(AppUser::new);

        user.setEmail(TEST_EMAIL);
        user.setPasswordHash(passwordEncoder.encode(TEST_PASSWORD));
        user.setFirstName("Playwright");
        user.setLastName("Student");
        user.setRole(Role.STUDENT);
        user.setActive(true);

        appUserRepository.save(user);
    }

    private void createTeacherTestUser() {
        AppUser user = appUserRepository
                .findByEmailIgnoreCase(TEST_EMAIL)
                .orElseGet(AppUser::new);

        user.setEmail(TEST_EMAIL);
        user.setPasswordHash(passwordEncoder.encode(TEST_PASSWORD));
        user.setFirstName("Playwright");
        user.setLastName("Teacher");
        user.setRole(Role.TEACHER);
        user.setActive(true);

        appUserRepository.save(user);
    }

    private void saveStudentScreenshot() throws IOException {
        Path screenshotPath = Path.of(
                "target",
                "playwright",
                "login-success.png"
        );

        Files.createDirectories(screenshotPath.getParent());

        page.screenshot(
                new Page.ScreenshotOptions()
                        .setPath(screenshotPath)
                        .setFullPage(true)
        );
    }

    private void saveStudentFailScreenshot() throws IOException {
        Path screenshotPath = Path.of(
                "target",
                "playwright",
                "login-fail.png"
        );

        Files.createDirectories(screenshotPath.getParent());

        page.screenshot(
                new Page.ScreenshotOptions()
                        .setPath(screenshotPath)
                        .setFullPage(true)
        );
    }

    private void saveTeacherScreenshot() throws IOException {
        Path screenshotPath = Path.of(
                "target",
                "playwright",
                "login-teacher-success.png"
        );

        Files.createDirectories(screenshotPath.getParent());

        page.screenshot(
                new Page.ScreenshotOptions()
                        .setPath(screenshotPath)
                        .setFullPage(true)
        );
    }

    private String applicationUrl(String path) {
        return "http://localhost:" + serverPort + path;
    }
}
