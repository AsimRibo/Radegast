package hr.asimr.radegast.domain.admin;

import hr.asimr.radegast.data.entities.AppUser;
import hr.asimr.radegast.data.entities.enums.Role;
import hr.asimr.radegast.data.repositories.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AppUserRepository appUserRepository;

    public List<AppUser> getManageableUsers() {
        return appUserRepository.findAll()
                .stream()
                .filter(this::isManageableUser)
                .sorted(
                        Comparator
                                .comparing(AppUser::getLastName)
                                .thenComparing(AppUser::getFirstName)
                )
                .toList();
    }

    public AppUser getManageableUser(Long userId) {
        return appUserRepository.findById(userId)
                .filter(this::isManageableUser)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User was not found"
                ));
    }

    private boolean isManageableUser(AppUser user) {
        return user.getRole() == Role.STUDENT
               || user.getRole() == Role.TEACHER;
    }

    @Transactional
    public void activateUser(Long userId) {
        updateUserStatus(userId, true);
    }

    @Transactional
    public void deactivateUser(Long userId) {
        updateUserStatus(userId, false);
    }

    private void updateUserStatus(Long userId, boolean active) {
        AppUser user = getManageableUser(userId);
        user.setActive(active);
        appUserRepository.save(user);
    }
}
