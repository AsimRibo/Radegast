package hr.asimr.radegast.controllers.grade;

import hr.asimr.radegast.data.entities.Course;
import hr.asimr.radegast.domain.grade.GradeService;
import hr.asimr.radegast.domain.grade.StudentGradeDto;
import hr.asimr.radegast.domain.grade.StudentProgressDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/student/courses")
public class StudentGradeController {

    private final GradeService gradeService;

    @GetMapping("/{courseId}/grades")
    public String viewGrades(
            @PathVariable Long courseId,
            Principal principal,
            Model model
    ) {
        Course course = gradeService.findCourseForStudent(
                courseId,
                principal.getName()
        );

        List<StudentGradeDto> grades = gradeService.findGradesForStudent(
                course,
                principal.getName()
        );

        StudentProgressDto progress = gradeService.calculateProgress(grades);

        model.addAttribute("course", course);
        model.addAttribute("grades", grades);
        model.addAttribute("progress", progress);

        return "student/grades";
    }
}
