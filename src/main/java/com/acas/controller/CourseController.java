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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {
    
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllCourses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String instructorId,
            @RequestParam(required = false) Boolean enrolled) {
        
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Course> coursePage;
        
        if (enrolled != null && enrolled) {
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            coursePage = courseRepository.findByStudentId(currentUser.getId(), pageable);
        } else if (instructorId != null) {
            coursePage = courseRepository.findByInstructorId(instructorId, pageable);
        } else if (search != null && !search.isEmpty()) {
            coursePage = courseRepository.searchCourses(search, pageable);
        } else {
            coursePage = courseRepository.findAll(pageable);
        }
        
        List<Map<String, Object>> courses = coursePage.getContent().stream()
                .map(this::mapCourseToDto)
                .collect(Collectors.toList());
        
        PaginationDto pagination = PaginationDto.of(
                coursePage.getTotalElements(),
                page,
                limit
        );
        
        Map<String, Object> data = new HashMap<>();
        data.put("courses", courses);
        data.put("pagination", pagination);
        
        return ResponseEntity.ok(ApiResponse.success(data));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCourseById(@PathVariable String id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        Map<String, Object> courseData = mapCourseToDtoDetail(course);
        
        Map<String, Object> data = new HashMap<>();
        data.put("course", courseData);
        
        return ResponseEntity.ok(ApiResponse.success(data));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createCourse(@RequestBody Map<String, Object> request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Course course = new Course();
        course.setName((String) request.get("name"));
        course.setDescription((String) request.get("description"));
        course.setInstructor(currentUser);
        
        if (request.containsKey("syllabus")) {
            course.setSyllabus((String) request.get("syllabus"));
        }
        
        courseRepository.save(course);
        
        Map<String, Object> courseData = mapCourseToDto(course);
        Map<String, Object> data = new HashMap<>();
        data.put("course", courseData);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(data, "Course created successfully"));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateCourse(
            @PathVariable String id,
            @RequestBody Map<String, Object> request) {
        
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        if (request.containsKey("name")) {
            course.setName((String) request.get("name"));
        }
        if (request.containsKey("description")) {
            course.setDescription((String) request.get("description"));
        }
        if (request.containsKey("syllabus")) {
            course.setSyllabus((String) request.get("syllabus"));
        }
        
        courseRepository.save(course);
        
        Map<String, Object> courseData = mapCourseToDto(course);
        Map<String, Object> data = new HashMap<>();
        data.put("course", courseData);
        
        return ResponseEntity.ok(ApiResponse.success(data, "Course updated successfully"));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable String id) {
        courseRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Course deleted successfully"));
    }
    
    private Map<String, Object> mapCourseToDto(Course course) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", course.getId());
        dto.put("name", course.getName());
        dto.put("description", course.getDescription());
        dto.put("instructorId", course.getInstructor() != null ? course.getInstructor().getId() : null);
        dto.put("instructorName", course.getInstructor() != null ? course.getInstructor().getName() : null);
        dto.put("studentsCount", course.getStudents().size());
        dto.put("createdAt", course.getCreatedAt());
        dto.put("updatedAt", course.getUpdatedAt());
        return dto;
    }
    
    private Map<String, Object> mapCourseToDtoDetail(Course course) {
        Map<String, Object> dto = mapCourseToDto(course);
        
        List<Map<String, Object>> students = course.getStudents().stream()
                .map(student -> {
                    Map<String, Object> s = new HashMap<>();
                    s.put("id", student.getId());
                    s.put("name", student.getName());
                    s.put("email", student.getEmail());
                    s.put("enrolledAt", course.getCreatedAt());
                    return s;
                })
                .collect(Collectors.toList());
        
        dto.put("students", students);
        dto.put("syllabus", course.getSyllabus());
        
        Map<String, Object> schedule = new HashMap<>();
        schedule.put("days", course.getScheduleDays());
        schedule.put("time", course.getScheduleTime());
        dto.put("schedule", schedule);
        
        return dto;
    }
}
