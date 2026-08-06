package hr.asimr.radegast.controllers.admin;

import hr.asimr.radegast.domain.admin.CourseEnrollmentService;
import hr.asimr.radegast.domain.course.EnrollmentOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/courses/{courseId}/enrollments")
@RequiredArgsConstructor
public class CourseEnrollmentController {

    private final CourseEnrollmentService courseEnrollmentService;

    @GetMapping
    public String listEnrollments(
            @PathVariable Long courseId,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            model.addAttribute(
                    "course",
                    courseEnrollmentService.findCourseEnrollments(courseId, authentication.getName())
            );

            return "courses/enrollments/list";
        } catch (EnrollmentOperationException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/courses";
        }
    }

    @PostMapping("/enroll")
    public String enrollStudent(
            @PathVariable Long courseId,
            @RequestParam Long studentId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            courseEnrollmentService.enrollStudent(courseId, studentId, authentication.getName());
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "The student has been enrolled successfully."
            );
        } catch (EnrollmentOperationException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:/courses/" + courseId + "/enrollments";
    }

    @PostMapping("/{studentId}/withdraw")
    public String withdrawStudent(
            @PathVariable Long courseId,
            @PathVariable Long studentId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            courseEnrollmentService.withdrawStudent(courseId, studentId, authentication.getName());
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "The student has been withdrawn from the course."
            );
        } catch (EnrollmentOperationException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:/courses/" + courseId + "/enrollments";
    }
}
