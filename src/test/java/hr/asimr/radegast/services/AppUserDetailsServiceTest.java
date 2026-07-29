package hr.asimr.radegast.services;


import hr.asimr.radegast.data.entities.AppUser;
import hr.asimr.radegast.data.entities.enums.Role;
import hr.asimr.radegast.data.repositories.AppUserRepository;
import hr.asimr.radegast.domain.user.AppUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AppUserDetailsServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    private AppUserDetailsService appUserDetailsService;

    @BeforeEach
    void setUp() {
        appUserDetailsService = new AppUserDetailsService(appUserRepository);
    }

    @Test
    void activeUserIsEnabled() {
        AppUser appUser = createUser(
                "roki@gmail.com",
                Role.STUDENT,
                true
        );

        when(appUserRepository.findByEmailIgnoreCase(
                "roki@gmail.com"
        )).thenReturn(Optional.of(appUser));

        UserDetails userDetails =
                appUserDetailsService.loadUserByUsername(
                        "roki@gmail.com"
                );

        assertThat(userDetails.isEnabled()).isTrue();

        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_STUDENT");
    }

    @Test
    void unknownEmailIsRejected() {
        when(appUserRepository.findByEmailIgnoreCase(
                "roki2@gmail.com"
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                appUserDetailsService.loadUserByUsername(
                        "roki2@gmail.com"
                )
        ).isInstanceOf(UsernameNotFoundException.class);
    }

    private AppUser createUser(
            String email,
            Role role,
            boolean active
    ) {
        AppUser appUser = new AppUser();

        appUser.setEmail(email);
        appUser.setPasswordHash("encoded-password");
        appUser.setFirstName("Roki");
        appUser.setLastName("Rokic");
        appUser.setRole(role);
        appUser.setActive(active);

        return appUser;
    }
}
