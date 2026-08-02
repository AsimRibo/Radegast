package hr.asimr.radegast.services;

import hr.asimr.radegast.data.entities.AppUser;
import hr.asimr.radegast.data.entities.enums.Role;
import hr.asimr.radegast.data.repositories.AppUserRepository;
import hr.asimr.radegast.domain.admin.AdminUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdminUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private AdminUserService adminUserService;

    @Test
    void activateUserMakesAccountActive() {
        AppUser teacher = createUser(
                "roki@gmail.com",
                "Roki",
                "Balboa",
                Role.TEACHER,
                false
        );
        when(appUserRepository.findById(1L))
                .thenReturn(Optional.of(teacher));

        adminUserService.activateUser(1L);

        assertThat(teacher.isActive()).isTrue();
        verify(appUserRepository).save(teacher);
    }

    @Test
    void getManageableUserByIdReturnsUser() {
        AppUser student = createUser(
                "roki@gmail.com",
                "Roki",
                "Balboa",
                Role.STUDENT,
                true
        );
        when(appUserRepository.findById(1L))
                .thenReturn(Optional.of(student));

        AppUser result = adminUserService.getManageableUser(1L);

        assertThat(result).isSameAs(student);
    }

    @Test
    void getManageableUserDoesNotReturnAdmin() {
        AppUser student = createUser(
                "roki@gmail.com",
                "Roki",
                "Balboa",
                Role.STUDENT,
                true
        );
        AppUser someAdmin = createUser(
                "admin@gmail.com",
                "Adminko",
                "Admic",
                Role.ADMIN,
                true
        );
        when(appUserRepository.findAll())
                .thenReturn(List.of(student, someAdmin));

        List<AppUser> result = adminUserService.getManageableUsers();

        assertThat(result).containsExactly(student);
    }

    private AppUser createUser(
            String email,
            String firstName,
            String lastName,
            Role role,
            boolean active
    ) {
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setActive(active);
        return user;
    }
}
