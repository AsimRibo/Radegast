package hr.asimr.radegast.controllers.registration;

import hr.asimr.radegast.domain.user.DuplicateEmailException;
import hr.asimr.radegast.domain.user.InvalidRegistrationRoleException;
import hr.asimr.radegast.domain.user.RegisterUserDto;
import hr.asimr.radegast.domain.user.UserRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegistrationController {

    private static final String REGISTRATION_VIEW = "registration/register";
    private static final String REGISTRATION_SUCCESS_VIEW = "registration/success";

    private final UserRegistrationService userRegistrationService;

    @GetMapping
    public String showRegistrationForm(@ModelAttribute("registerUserDto") RegisterUserDto registerUserDto) {
        return REGISTRATION_VIEW;
    }

    @GetMapping("/success")
    public String showRegistrationSuccess() {
        return REGISTRATION_SUCCESS_VIEW;
    }

    @PostMapping
    public String registerUser(
            @Valid
            @ModelAttribute("registerUserDto")
            RegisterUserDto registerUserDto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return REGISTRATION_VIEW;
        }

        try {
            userRegistrationService.registerUser(registerUserDto);
        } catch (DuplicateEmailException exception) {
            bindingResult.rejectValue(
                    "email",
                    "duplicate",
                    exception.getMessage()
            );

            return REGISTRATION_VIEW;
        } catch (InvalidRegistrationRoleException exception) {
            bindingResult.rejectValue(
                    "role",
                    "invalid",
                    exception.getMessage()
            );

            return REGISTRATION_VIEW;
        }

        return "redirect:/register/success";
    }
}
