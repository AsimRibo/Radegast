package hr.asimr.radegast.integration;

import hr.asimr.radegast.config.SecurityConfig;
import hr.asimr.radegast.controllers.admin.AdminUserController;
import hr.asimr.radegast.data.entities.AppUser;
import hr.asimr.radegast.data.entities.enums.Role;
import hr.asimr.radegast.domain.admin.AdminUserService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
@Import(SecurityConfig.class)
public class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminUserService adminUserService;

    private static final String PASSWORD = "password";

    @Test
    void adminitratorCanViewUsers() throws Exception {
        AppUser student = createUser(
                1L,
                "ivan@gmail.local",
                Role.STUDENT,
                true
        );
        when(adminUserService.getManageableUsers())
                .thenReturn(List.of(student));

        mockMvc.perform(
                        get("/admin/users")
                                .with(user("admin@gmail.com")
                                        .roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"))
                .andExpect(model().attribute("users", List.of(student)));
    }

    @Test
    void administratorCanDeactivateUser() throws Exception {
        mockMvc.perform(
                        post("/admin/users/1/deactivate")
                                .with(user("admin@gmail.com")
                                        .roles("ADMIN")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users/1"))
                .andExpect(flash().attribute(
                        "successMessage",
                        "The user account with ID: 1 has been deactivated."
                ));

        verify(adminUserService).deactivateUser(1L);
    }

    private AppUser createUser(
            Long id,
            String email,
            Role role,
            boolean active
    ) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setEmail(email);
        user.setFirstName("Ivan");
        user.setLastName("Ivic");
        user.setRole(role);
        user.setActive(active);
        return user;
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

                    User.withUsername("admin@gmail.com")
                            .password(passwordEncoder.encode(PASSWORD))
                            .roles("ADMIN")
                            .build(),

                    User.withUsername("ivan@gmail.com")
                            .password(passwordEncoder.encode(PASSWORD))
                            .roles("STUDENT")
                            .build()
            );
        }
    }
}
