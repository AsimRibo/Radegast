package hr.asimr.radegast.controllers.assessment;

import hr.asimr.radegast.domain.assessment.AssessmentFormDto;
import hr.asimr.radegast.domain.assessment.AssessmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/courses/{courseId}/assessments")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;

    @GetMapping
    public String listAssessments(@PathVariable Long courseId, Authentication authentication, Model model) {
        model.addAttribute("courseId", courseId);
        model.addAttribute("assessments", assessmentService.findAssessmentsForCourse(courseId, authentication.getName()));

        return "assessments/list";
    }

    @GetMapping("/new")
    public String createForm(@PathVariable Long courseId, Authentication authentication, Model model) {
        assessmentService.verifyCourseManagementAccess(courseId, authentication.getName());

        prepareCreateForm(model, courseId, new AssessmentFormDto());

        return "assessments/form";
    }

    @PostMapping
    public String createAssessment(
            @PathVariable Long courseId,
            @Valid @ModelAttribute("assessmentForm") AssessmentFormDto assessmentForm,
            BindingResult bindingResult,
            Authentication authentication,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            assessmentService.verifyCourseManagementAccess(courseId, authentication.getName());
            prepareCreateForm(model, courseId, assessmentForm);

            return "assessments/form";
        }

        assessmentService.createAssessment(courseId, assessmentForm, authentication.getName());

        return "redirect:/courses/" + courseId + "/assessments";
    }

    @GetMapping("/{assessmentId}/edit")
    public String editForm(
            @PathVariable Long courseId,
            @PathVariable Long assessmentId,
            Authentication authentication,
            Model model
    ) {
        AssessmentFormDto assessmentForm = assessmentService.findAssessmentForEdit(
                courseId, assessmentId, authentication.getName());

        prepareEditForm(model, courseId, assessmentId, assessmentForm);

        return "assessments/form";
    }

    @PostMapping("/{assessmentId}/edit")
    public String updateAssessment(
            @PathVariable Long courseId,
            @PathVariable Long assessmentId,
            @Valid @ModelAttribute("assessmentForm") AssessmentFormDto assessmentForm,
            BindingResult bindingResult,
            Authentication authentication,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            assessmentService.verifyAssessmentManagementAccess(courseId, assessmentId, authentication.getName());
            prepareEditForm(model, courseId, assessmentId, assessmentForm);

            return "assessments/form";
        }

        assessmentService.updateAssessment(courseId, assessmentId, assessmentForm, authentication.getName());

        return "redirect:/courses/" + courseId + "/assessments";
    }

    private void prepareCreateForm(Model model, Long courseId, AssessmentFormDto assessmentForm) {
        model.addAttribute("assessmentForm", assessmentForm);
        model.addAttribute("courseId", courseId);
        model.addAttribute("formTitle", "Create assessment");
        model.addAttribute("submitLabel", "Create");
        model.addAttribute("formAction", "/courses/" + courseId + "/assessments");
    }

    private void prepareEditForm(Model model, Long courseId, Long assessmentId, AssessmentFormDto assessmentForm) {
        model.addAttribute("assessmentForm", assessmentForm);
        model.addAttribute("courseId", courseId);
        model.addAttribute("formTitle", "Edit assessment");
        model.addAttribute("submitLabel", "Save changes");
        model.addAttribute("formAction", "/courses/" + courseId + "/assessments/" + assessmentId + "/edit");
    }
}
