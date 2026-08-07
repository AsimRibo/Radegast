package hr.asimr.radegast.data.repositories;

import hr.asimr.radegast.data.entities.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GradeRepository  extends JpaRepository<Grade, Long> {

    Optional<Grade> findByAssessment_IdAndStudent_Id(Long assessmentId, Long studentId);

    List<Grade> findAllByAssessment_Id(Long assessmentId);

    List<Grade> findAllByStudent_IdAndAssessment_Course_Id(Long studentId, Long courseId);
}
