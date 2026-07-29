package hr.asimr.radegast.integration;

import hr.asimr.radegast.config.SecurityConfig;
import hr.asimr.radegast.controllers.authentication.LoginController;
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
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({LoginController.class})
@Import({SecurityConfig.class})
public class LoginControllerTest {

    private static final String PASSWORD = "password";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginPageIsPublic() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("authentication/login"));
    }

    @Test
    void activeUserCanLogIn() throws Exception {
        mockMvc.perform(
                        formLogin("/login")
                                .user("email", "roki@gmail.com")
                                .password("password", PASSWORD)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(authenticated()
                        .withUsername("roki@gmail.com"));
    }

    @Test
    void inactiveUserCannotLogIn() throws Exception {
        mockMvc.perform(
                        formLogin("/login")
                                .user("email", "rocky@gmail.com")
                                .password("password", PASSWORD)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    void unknownEmailIsRejected() throws Exception {
        mockMvc.perform(
                        formLogin("/login")
                                .user("email", "roki2@gmail.com")
                                .password("password", PASSWORD)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
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

                    User.withUsername("roki@gmail.com")
                            .password(passwordEncoder.encode(PASSWORD))
                            .roles("STUDENT")
                            .build(),

                    User.withUsername("rocky@gmail.com")
                            .password(passwordEncoder.encode(PASSWORD))
                            .roles("STUDENT")
                            .disabled(true)
                            .build()
            );
        }
    }
}
