package hr.asimr.radegast.controllers.admin;

import hr.asimr.radegast.domain.admin.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public String showUsers(Model model) {
        model.addAttribute(
                "users",
                adminUserService.getManageableUsers()
        );

        return "admin/users";
    }

    @GetMapping("/{userId}")
    public String showUser(
            @PathVariable Long userId,
            Model model
    ) {
        model.addAttribute(
                "user",
                adminUserService.getManageableUser(userId)
        );

        return "admin/user-details";
    }

    @PostMapping("/{userId}/activate")
    public String activateUser(
            @PathVariable Long userId,
            RedirectAttributes redirectAttributes
    ) {
        adminUserService.activateUser(userId);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "The user account with ID: " + userId + " has been activated."
        );

        return "redirect:/admin/users/" + userId;
    }

    @PostMapping("/{userId}/deactivate")
    public String deactivateUser(
            @PathVariable Long userId,
            RedirectAttributes redirectAttributes
    ) {
        adminUserService.deactivateUser(userId);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "The user account with ID " + userId + " has been deactivated."
        );

        return "redirect:/admin/users/" + userId;
    }
}
