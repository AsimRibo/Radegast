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
            return "redirect:/student/dashboard";
        }

        if (hasAuthority(authentication, "ROLE_TEACHER")) {
            return "redirect:/teacher/dashboard";
        }

        if (hasAuthority(authentication, "ROLE_ADMIN")) {
            return "redirect:/admin/users";
        }

        throw new AccessDeniedException(
                "Something went wrong."
        );
    }

    @GetMapping("/student/dashboard")
    public String showStudentDashboard(
            Authentication authentication,
            Model model
    ) {
        prepareDashboard(
                model,
                authentication,
                "Student dashboard",
                "View your courses, enrolments, assessments and progress."
        );

        return "dashboard";
    }

    @GetMapping("/teacher/dashboard")
    public String showTeacherDashboard(
            Authentication authentication,
            Model model
    ) {
        prepareDashboard(
                model,
                authentication,
                "Teacher dashboard",
                "Manage your courses, assessments, grades and feedback."
        );

        return "dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String showAdminDashboard(
            Authentication authentication,
            Model model
    ) {
        prepareDashboard(
                model,
                authentication,
                "Administrator dashboard",
                "Manage users, courses, enrolments and administrative actions."
        );

        return "dashboard";
    }

    private void prepareDashboard(
            Model model,
            Authentication authentication,
            String title,
            String description
    ) {
        model.addAttribute("title", title);
        model.addAttribute("description", description);
        model.addAttribute("email", authentication.getName());
    }

    private boolean hasAuthority(
            Authentication authentication,
            String requiredAuthority
    ) {
        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals(requiredAuthority)
                );
    }
}
