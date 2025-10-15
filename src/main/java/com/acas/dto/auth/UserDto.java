package com.acas.dto.auth;

import com.acas.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String id;
    private String email;
    private String name;
    private User.UserRole role;
    private String avatar;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
