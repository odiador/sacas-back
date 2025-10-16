package com.acas.config;

import com.acas.model.User;
import com.acas.model.User.UserRole;
import com.acas.model.User.UserStatus;
import com.acas.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Initializing database with test users...");
            
            // Create Admin user
            User admin = new User();
            admin.setEmail("admin@acas.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setName("Admin User");
            admin.setRole(UserRole.ADMIN);
            admin.setStatus(UserStatus.ACTIVE);
            userRepository.save(admin);
            log.info("Created admin user: admin@acas.com / admin123");
            
            // Create Teacher user
            User teacher = new User();
            teacher.setEmail("teacher@acas.com");
            teacher.setPassword(passwordEncoder.encode("teacher123"));
            teacher.setName("John Teacher");
            teacher.setRole(UserRole.TEACHER);
            teacher.setStatus(UserStatus.ACTIVE);
            userRepository.save(teacher);
            log.info("Created teacher user: teacher@acas.com / teacher123");
            
            // Create Student user
            User student = new User();
            student.setEmail("student@acas.com");
            student.setPassword(passwordEncoder.encode("student123"));
            student.setName("Jane Student");
            student.setRole(UserRole.STUDENT);
            student.setStatus(UserStatus.ACTIVE);
            userRepository.save(student);
            log.info("Created student user: student@acas.com / student123");
            
            log.info("Database initialization completed!");
        } else {
            log.info("Database already contains data, skipping initialization.");
        }
    }
}
