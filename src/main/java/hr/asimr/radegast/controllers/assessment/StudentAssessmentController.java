package hr.asimr.radegast.controllers.assessment;

import hr.asimr.radegast.domain.assessment.AssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/student/courses/{courseId}/assessments")
@RequiredArgsConstructor
public class StudentAssessmentController {

    private final AssessmentService assessmentService;

    @GetMapping
    public String listAssessments(@PathVariable Long courseId, Authentication authentication, Model model) {
        model.addAttribute("assessments", assessmentService.findAssessmentsForStudent(courseId, authentication.getName()));

        return "student/assessments";
    }
}
