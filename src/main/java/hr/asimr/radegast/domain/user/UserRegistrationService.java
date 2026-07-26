package hr.asimr.radegast.domain.user;

import hr.asimr.radegast.data.entities.AppUser;
import hr.asimr.radegast.data.entities.enums.Role;
import hr.asimr.radegast.data.repositories.AppUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRegistrationService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AppUser registerUser(RegisterUserDto registerUserDto) {
        validateRole(registerUserDto.getRole());

        String normalizedEmail = normalizeEmail(registerUserDto.getEmail());

        validateEmail(normalizedEmail);

        AppUser user = new AppUser();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(
                passwordEncoder.encode(registerUserDto.getPassword())
        );
        user.setFirstName(registerUserDto.getFirstName().trim());
        user.setLastName(registerUserDto.getLastName().trim());
        user.setRole(registerUserDto.getRole());
        user.setActive(true);

        return appUserRepository.save(user);
    }

    private void validateEmail(String normalizedEmail) {
        if (appUserRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateEmailException();
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private void validateRole(Role role) {
        boolean isPublicRegistrationRole = role == Role.TEACHER || role == Role.STUDENT;

        if (!isPublicRegistrationRole) {
            throw new InvalidRegistrationRoleException();
        }
    }
}
