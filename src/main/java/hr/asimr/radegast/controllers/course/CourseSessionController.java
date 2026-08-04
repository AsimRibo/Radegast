package hr.asimr.radegast.controllers.course;

import hr.asimr.radegast.domain.course.CourseSessionFormDto;
import hr.asimr.radegast.domain.course.CourseSessionService;
import hr.asimr.radegast.domain.course.CourseSessionCourseDto;
import hr.asimr.radegast.domain.course.CourseSessionsPageDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/courses/{courseId}/sessions")
@RequiredArgsConstructor
public class CourseSessionController {

    private final CourseSessionService courseSessionService;

    @GetMapping
    public String listSessions(@PathVariable Long courseId, Authentication authentication, Model model) {
        CourseSessionsPageDto page = courseSessionService.findSessionsForCourse(courseId, authentication.getName());

        model.addAttribute("course", page.course());
        model.addAttribute("sessions", page.sessions());
        model.addAttribute("canManage", page.canManage());

        return "coursesessions/list";
    }

    @GetMapping("/new")
    public String showCreateSessionForm(@PathVariable Long courseId, Authentication authentication, Model model) {
        CourseSessionCourseDto course = courseSessionService.getCourseForSessionManagement(courseId, authentication.getName());

        model.addAttribute("sessionForm", new CourseSessionFormDto());

        addFormAttributes(
                model,
                course,
                "Schedule session",
                "Schedule session",
                "/courses/" + courseId + "/sessions"
        );

        return "coursesessions/form";
    }

    @PostMapping
    public String createSession(
            @PathVariable Long courseId,
            @Valid
            @ModelAttribute("sessionForm")
            CourseSessionFormDto sessionForm,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        validateTimeRange(sessionForm, bindingResult);

        if (bindingResult.hasErrors()) {
            CourseSessionCourseDto course = courseSessionService.getCourseForSessionManagement(courseId, authentication.getName());

            addFormAttributes(
                    model,
                    course,
                    "Schedule session",
                    "Schedule session",
                    "/courses/" + courseId + "/sessions"
            );

            return "coursesessions/form";
        }

        courseSessionService.createSession(courseId, sessionForm, authentication.getName());

        redirectAttributes.addFlashAttribute("successMessage", "Session scheduled successfully.");

        return redirectToSessions(courseId);
    }

    @GetMapping("/{sessionId}/edit")
    public String showEditSessionForm(
            @PathVariable Long courseId,
            @PathVariable Long sessionId,
            Authentication authentication,
            Model model
    ) {
        CourseSessionCourseDto course = courseSessionService.getCourseForSessionManagement(courseId, authentication.getName());

        CourseSessionFormDto sessionForm = courseSessionService.getSessionForEditing(courseId, sessionId, authentication.getName());

        model.addAttribute("sessionForm", sessionForm);

        addFormAttributes(
                model,
                course,
                "Edit session",
                "Save changes",
                "/courses/" + courseId + "/sessions/" + sessionId
        );

        return "coursesessions/form";
    }

    @PostMapping("/{sessionId}")
    public String updateSession(
            @PathVariable Long courseId,
            @PathVariable Long sessionId,
            @Valid
            @ModelAttribute("sessionForm")
            CourseSessionFormDto sessionForm,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        validateTimeRange(sessionForm, bindingResult);

        if (bindingResult.hasErrors()) {
            CourseSessionCourseDto course = courseSessionService.getCourseForSessionManagement(courseId, authentication.getName());

            addFormAttributes(
                    model,
                    course,
                    "Edit session",
                    "Save changes",
                    "/courses/" + courseId + "/sessions/" + sessionId
            );

            return "coursesessions/form";
        }

        courseSessionService.updateSession(
                courseId,
                sessionId,
                sessionForm,
                authentication.getName()
        );

        redirectAttributes.addFlashAttribute("successMessage", "Session updated successfully.");

        return redirectToSessions(courseId);
    }

    @PostMapping("/{sessionId}/cancel")
    public String cancelSession(
            @PathVariable Long courseId,
            @PathVariable Long sessionId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        courseSessionService.cancelSession(courseId, sessionId, authentication.getName());

        redirectAttributes.addFlashAttribute("successMessage", "Session cancelled successfully.");

        return redirectToSessions(courseId);
    }

    private void validateTimeRange(CourseSessionFormDto sessionForm, BindingResult bindingResult) {
        if (
                sessionForm.getStartsAt() != null
                && sessionForm.getEndsAt() != null
                && !sessionForm.getEndsAt().isAfter(sessionForm.getStartsAt())
        ) {
            bindingResult.rejectValue(
                    "endsAt",
                    "invalidTimeRange",
                    "End time must be later than start time."
            );
        }
    }

    private void addFormAttributes(
            Model model,
            CourseSessionCourseDto course,
            String formTitle,
            String submitLabel,
            String formAction
    ) {
        model.addAttribute("course", course);
        model.addAttribute("formTitle", formTitle);
        model.addAttribute("submitLabel", submitLabel);
        model.addAttribute("formAction", formAction);
    }

    private String redirectToSessions(Long courseId) {
        return "redirect:/courses/" + courseId + "/sessions";
    }
}
