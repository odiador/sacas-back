package com.acas.controller;

import com.acas.dto.ApiResponse;
import com.acas.dto.PaginationDto;
import com.acas.model.Course;
import com.acas.model.User;
import com.acas.repository.CourseRepository;
import com.acas.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
public class StudentController {
    
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllStudents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String courseId) {
        
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<User> studentPage;
        
        if (search != null && !search.isEmpty()) {
            studentPage = userRepository.searchUsers(search, pageable);
        } else {
            studentPage = userRepository.findByRole(User.UserRole.STUDENT, pageable);
        }
        
        List<Map<String, Object>> students = studentPage.getContent().stream()
                .filter(user -> user.getRole() == User.UserRole.STUDENT)
                .map(this::mapUserToStudent)
                .collect(Collectors.toList());
        
        PaginationDto pagination = PaginationDto.of(
                studentPage.getTotalElements(),
                page,
                limit
        );
        
        Map<String, Object> data = new HashMap<>();
        data.put("students", students);
        data.put("pagination", pagination);
        
        return ResponseEntity.ok(ApiResponse.success(data));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStudentById(@PathVariable String id) {
        User student = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        if (student.getRole() != User.UserRole.STUDENT) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("INVALID_ROLE", "User is not a student"));
        }
        
        Map<String, Object> studentData = mapUserToStudentDetail(student);
        
        Map<String, Object> data = new HashMap<>();
        data.put("student", studentData);
        
        return ResponseEntity.ok(ApiResponse.success(data));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createStudent(@RequestBody Map<String, String> request) {
        // Implementation for creating a student
        // This would hash the password and create the user
        
        Map<String, Object> data = new HashMap<>();
        data.put("message", "Student creation endpoint - implement with UserService");
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(data, "Student created successfully"));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateStudent(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        
        User student = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        if (request.containsKey("name")) {
            student.setName(request.get("name"));
        }
        if (request.containsKey("email")) {
            student.setEmail(request.get("email"));
        }
        
        userRepository.save(student);
        
        Map<String, Object> studentData = mapUserToStudent(student);
        Map<String, Object> data = new HashMap<>();
        data.put("student", studentData);
        
        return ResponseEntity.ok(ApiResponse.success(data, "Student updated successfully"));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable String id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Student deleted successfully"));
    }
    
    @PostMapping("/{id}/enroll")
    public ResponseEntity<ApiResponse<Map<String, Object>>> enrollStudent(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        
        String courseId = request.get("courseId");
        
        User student = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        course.getStudents().add(student);
        courseRepository.save(course);
        
        Map<String, Object> enrollment = new HashMap<>();
        enrollment.put("studentId", id);
        enrollment.put("courseId", courseId);
        enrollment.put("enrolledAt", java.time.LocalDateTime.now());
        
        Map<String, Object> data = new HashMap<>();
        data.put("enrollment", enrollment);
        
        return ResponseEntity.ok(ApiResponse.success(data, "Student enrolled successfully"));
    }
    
    @DeleteMapping("/{id}/enroll/{courseId}")
    public ResponseEntity<ApiResponse<Void>> unenrollStudent(
            @PathVariable String id,
            @PathVariable String courseId) {
        
        User student = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        course.getStudents().remove(student);
        courseRepository.save(course);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Student unenrolled successfully"));
    }
    
    private Map<String, Object> mapUserToStudent(User user) {
        Map<String, Object> student = new HashMap<>();
        student.put("id", user.getId());
        student.put("name", user.getName());
        student.put("email", user.getEmail());
        student.put("enrolledCourses", user.getEnrolledCourses().stream()
                .map(Course::getId)
                .collect(Collectors.toList()));
        student.put("enrolledCoursesCount", user.getEnrolledCourses().size());
        student.put("createdAt", user.getCreatedAt());
        student.put("updatedAt", user.getUpdatedAt());
        return student;
    }
    
    private Map<String, Object> mapUserToStudentDetail(User user) {
        Map<String, Object> student = mapUserToStudent(user);
        
        List<Map<String, Object>> courses = user.getEnrolledCourses().stream()
                .map(course -> {
                    Map<String, Object> c = new HashMap<>();
                    c.put("id", course.getId());
                    c.put("name", course.getName());
                    c.put("enrolledAt", course.getCreatedAt());
                    return c;
                })
                .collect(Collectors.toList());
        
        student.put("enrolledCourses", courses);
        student.put("grades", List.of()); // Placeholder for grades
        
        return student;
    }
}
