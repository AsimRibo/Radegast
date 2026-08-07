package hr.asimr.radegast.controllers.grade;

import hr.asimr.radegast.data.entities.Assessment;
import hr.asimr.radegast.domain.grade.GradeFormDto;
import hr.asimr.radegast.domain.grade.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/courses/assessments")
public class GradeController {

    private final GradeService gradeService;

    @GetMapping("/{assessmentId}/grades")
    public String viewGrades(@PathVariable Long assessmentId, Principal principal, Model model) {
        Assessment assessment = gradeService.findAssessmentForTeacher(assessmentId, principal.getName());

        model.addAttribute("assessment", assessment);
        model.addAttribute("students", gradeService.findStudentsForGrading(assessment));

        return "assessments/grades";
    }

    @PostMapping("/{assessmentId}/grades")
    public String saveGrade(
            @PathVariable Long assessmentId,
            GradeFormDto form,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        try {
            gradeService.saveGrade(assessmentId, form, principal.getName());

            redirectAttributes.addFlashAttribute("message", "Grade saved successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    exception.getMessage()
            );
        }

        return "redirect:/courses/assessments/" + assessmentId + "/grades";
    }
}
