package com.acas.repository;

import com.acas.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, String> {
    
    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.course.id = :courseId")
    Optional<Enrollment> findByStudentIdAndCourseId(
        @Param("studentId") String studentId,
        @Param("courseId") String courseId
    );
    
    boolean existsByStudentIdAndCourseId(String studentId, String courseId);
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE " +
           "FUNCTION('MONTH', e.enrolledAt) = FUNCTION('MONTH', CURRENT_DATE) AND " +
           "FUNCTION('YEAR', e.enrolledAt) = FUNCTION('YEAR', CURRENT_DATE)")
    long countEnrollmentsThisMonth();
}
