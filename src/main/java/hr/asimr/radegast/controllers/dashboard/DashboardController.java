package hr.asimr.radegast.controllers.dashboard;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String redirectToRoleDashoard(Authentication authentication) {

        if (hasAuthority(authentication, "ROLE_STUDENT")) {
            return "redirect:/student/courses";
        }

        if (hasAuthority(authentication, "ROLE_TEACHER")) {
            return "redirect:/courses";
        }

        if (hasAuthority(authentication, "ROLE_ADMIN")) {
            return "redirect:/admin/users";
        }

        throw new AccessDeniedException("Something went wrong.");
    }

    private boolean hasAuthority(
            Authentication authentication,
            String requiredAuthority
    ) {
        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals(requiredAuthority)
                );
    }
}
