package hr.asimr.radegast.data.repositories;

import hr.asimr.radegast.data.entities.CourseSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CourseSessionRepository extends JpaRepository<CourseSession, Long> {

    List<CourseSession> findAllByCourse_IdOrderByStartsAtAsc(Long courseId);

    List<CourseSession> findAllByCourse_IdAndStartsAtGreaterThanEqualOrderByStartsAtAsc(Long courseId, LocalDateTime startsAt);

    Optional<CourseSession> findByIdAndCourse_Id(Long sessionId, Long courseId);
}
