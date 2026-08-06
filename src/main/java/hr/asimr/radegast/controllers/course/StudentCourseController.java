package hr.asimr.radegast.controllers.course;

import hr.asimr.radegast.domain.course.EnrollmentOperationException;
import hr.asimr.radegast.domain.course.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/student/courses")
@RequiredArgsConstructor
public class StudentCourseController {

    private final EnrollmentService enrollmentService;

    @GetMapping
    public String listCourses(Authentication authentication, Model model) {
        model.addAttribute(
                "courses",
                enrollmentService.findCoursesForStudent(authentication.getName())
        );

        return "student/courses/list";
    }

    @PostMapping("/{courseId}/enroll")
    public String enroll(
            @PathVariable Long courseId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            enrollmentService.enroll(courseId, authentication.getName());
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "You have successfully enrolled in the course."
            );
        } catch (EnrollmentOperationException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:/student/courses";
    }

    @PostMapping("/{courseId}/withdraw")
    public String withdraw(
            @PathVariable Long courseId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            enrollmentService.withdraw(courseId, authentication.getName());
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "You have withdrawn from the course."
            );
        } catch (EnrollmentOperationException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:/student/courses";
    }

    @GetMapping("/{courseId}/sessions")
    public String viewSessions(
            @PathVariable Long courseId,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            model.addAttribute(
                    "course",
                    enrollmentService.findEnrolledCourseDetails(courseId, authentication.getName())
            );

            return "student/courses/sessions";
        } catch (EnrollmentOperationException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());

            return "redirect:/student/courses";
        }
    }
}
