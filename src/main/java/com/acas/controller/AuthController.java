package com.acas.controller;

import com.acas.dto.ApiResponse;
import com.acas.dto.auth.LoginRequest;
import com.acas.dto.auth.LoginResponse;
import com.acas.dto.auth.UserDto;
import com.acas.model.User;
import com.acas.repository.UserRepository;
import com.acas.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        // Load user details
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        
        // Get user from database
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        // Generate JWT token
        final String jwt = jwtUtil.generateToken(userDetails, user.getId(), user.getRole().name());
        
        // Set JWT in HTTP-only cookie
        Cookie cookie = new Cookie("token", jwt);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true in production with HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        response.addCookie(cookie);
        
        // Prepare response
        UserDto userDto = new UserDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getAvatar(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
        
        LoginResponse loginResponse = new LoginResponse(userDto, jwt);
        
        return ResponseEntity.ok(ApiResponse.success(loginResponse, "Login successful"));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        // Clear cookie
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
    }
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserDto userDto = new UserDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getAvatar(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
        
        return ResponseEntity.ok(ApiResponse.success(userDto));
    }
}
