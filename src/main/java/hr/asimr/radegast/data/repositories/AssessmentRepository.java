package hr.asimr.radegast.data.repositories;

import hr.asimr.radegast.data.entities.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    List<Assessment> findAllByCourse_IdOrderByDueAtAsc(Long courseId);
}
