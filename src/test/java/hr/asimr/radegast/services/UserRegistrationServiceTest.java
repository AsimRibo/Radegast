package hr.asimr.radegast.services;

import hr.asimr.radegast.data.entities.AppUser;
import hr.asimr.radegast.data.entities.enums.Role;
import hr.asimr.radegast.data.repositories.AppUserRepository;
import hr.asimr.radegast.domain.user.InvalidRegistrationRoleException;
import hr.asimr.radegast.domain.user.RegisterUserDto;
import hr.asimr.radegast.domain.user.UserRegistrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserRegistrationServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserRegistrationService userRegistrationService;

    @Test
    void registersStudentWithEncodedPassword() {
        RegisterUserDto dto = createValidUserDto(Role.STUDENT, "student@gmail.com");

        when(
                appUserRepository.existsByEmailIgnoreCase(
                        "student@gmail.com"
                )
        ).thenReturn(false);

        when(passwordEncoder.encode("Password123"))
                .thenReturn("encoded-password");

        when(appUserRepository.save(any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AppUser registeredUser =
                userRegistrationService.registerUser(dto);

        assertThat(registeredUser.getEmail())
                .isEqualTo("student@gmail.com");

        assertThat(registeredUser.getPasswordHash())
                .isEqualTo("encoded-password");

        assertThat(registeredUser.getFirstName())
                .isEqualTo("John");

        assertThat(registeredUser.getLastName())
                .isEqualTo("Smith");

        assertThat(registeredUser.getRole())
                .isEqualTo(Role.STUDENT);

        assertThat(registeredUser.isActive())
                .isTrue();

        verify(passwordEncoder).encode("Password123");
        verify(appUserRepository).save(registeredUser);
    }

    @Test
    void registersTeacher() {
        RegisterUserDto dto = createValidUserDto(Role.TEACHER, "teacher@gmail.com");

        when(
                appUserRepository.existsByEmailIgnoreCase(
                        "teacher@gmail.com"
                )
        ).thenReturn(false);

        when(passwordEncoder.encode("Password123"))
                .thenReturn("encoded-password");

        when(appUserRepository.save(any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AppUser registeredUser =
                userRegistrationService.registerUser(dto);

        assertThat(registeredUser.getRole())
                .isEqualTo(Role.TEACHER);

        assertThat(registeredUser.isActive())
                .isTrue();
    }

    @Test
    void rejectsAdministratorRegistration() {
        RegisterUserDto dto = createValidUserDto(Role.ADMIN, "admin@gmail.com");

        assertThatThrownBy(
                () -> userRegistrationService.registerUser(dto)
        )
                .isInstanceOf(
                        InvalidRegistrationRoleException.class
                )
                .hasMessage(
                        "Wrong message."
                );

        verifyNoInteractions(
                appUserRepository,
                passwordEncoder
        );
    }

    private RegisterUserDto createValidUserDto(Role role, String email) {
        RegisterUserDto dto = new RegisterUserDto();
        dto.setEmail(email);
        dto.setPassword("Password123");
        dto.setFirstName("John  ");
        dto.setLastName("Smith");
        dto.setRole(role);

        return dto;
    }
}
