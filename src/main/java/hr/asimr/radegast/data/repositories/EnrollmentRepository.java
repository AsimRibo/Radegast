package hr.asimr.radegast.data.repositories;

import hr.asimr.radegast.data.entities.Enrollment;
import hr.asimr.radegast.data.entities.enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Optional<Enrollment> findByStudent_IdAndCourse_Id(Long studentId, Long courseId);

    List<Enrollment> findAllByStudent_Id(Long studentId);

    long countByCourse_IdAndStatus(Long courseId, EnrollmentStatus status);

    List<Enrollment> findAllByCourse_IdOrderByStudent_LastNameAscStudent_FirstNameAsc(Long courseId);
}
