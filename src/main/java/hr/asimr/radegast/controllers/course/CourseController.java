package hr.asimr.radegast.controllers.course;

import hr.asimr.radegast.domain.course.CourseCodeAlreadyExistsException;
import hr.asimr.radegast.domain.course.CourseFormDto;
import hr.asimr.radegast.domain.course.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private static final String COURSE_FORM_ATTRIBUTE = "courseForm";

    private static final String COURSE_LIST_VIEW = "courses/list";

    private static final String COURSE_FORM_VIEW = "courses/form";

    private final CourseService courseService;

    @GetMapping
    public String showCourses(Authentication authentication, Model model) {
        model.addAttribute(
                "courses",
                courseService.findCoursesVisibleToAuthenticatedUser(authentication.getName())
        );

        model.addAttribute(
                "canCreateCourse",
                hasRole(authentication, "ROLE_TEACHER")
        );

        model.addAttribute(
                "showTeacherColumn",
                hasRole(authentication, "ROLE_ADMIN")
        );

        return COURSE_LIST_VIEW;
    }

    @GetMapping("/new")
    public String showCreateCourseForm(Model model) {
        model.addAttribute(
                COURSE_FORM_ATTRIBUTE,
                new CourseFormDto()
        );

        addFormAttributes(model);

        return COURSE_FORM_VIEW;
    }

    @PostMapping
    public String createCourse(
            @Valid
            @ModelAttribute(COURSE_FORM_ATTRIBUTE)
            CourseFormDto courseForm,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            addFormAttributes(model);
            return COURSE_FORM_VIEW;
        }

        try {
            courseService.createCourse(courseForm, authentication.getName());
        } catch (
                CourseCodeAlreadyExistsException exception
        ) {
            bindingResult.rejectValue(
                    "code",
                    "duplicate",
                    exception.getMessage()
            );

            addFormAttributes(model);

            return COURSE_FORM_VIEW;
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Course created successfully."
        );

        return "redirect:/courses";
    }

    private void addFormAttributes(Model model) {
        model.addAttribute(
                "formTitle",
                "Create course"
        );

        model.addAttribute(
                "formAction",
                "/courses"
        );

        model.addAttribute(
                "submitLabel",
                "Create course"
        );
    }

    private boolean hasRole(
            Authentication authentication,
            String role
    ) {
        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals(role)
                );
    }
}
