package com.acas.repository;

import com.acas.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {
    
    @Query("SELECT c FROM Course c WHERE c.instructor.id = :instructorId")
    Page<Course> findByInstructorId(@Param("instructorId") String instructorId, Pageable pageable);
    
    @Query("SELECT c FROM Course c JOIN c.students s WHERE s.id = :studentId")
    Page<Course> findByStudentId(@Param("studentId") String studentId, Pageable pageable);
    
    @Query("SELECT c FROM Course c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Course> searchCourses(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT c FROM Course c ORDER BY SIZE(c.students) DESC")
    List<Course> findTopCoursesByEnrollment(Pageable pageable);
}
