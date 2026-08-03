package hr.asimr.radegast.data.repositories;

import hr.asimr.radegast.data.entities.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByCode(String code);

    List<Course> findAllByOrderByCreatedAtDesc();

    List<Course> findAllByTeacher_IdOrderByCreatedAtDesc(
            Long teacherId
    );
}
