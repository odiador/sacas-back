package com.acas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private ErrorDetails error;
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null);
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, null);
    }
    
    public static <T> ApiResponse<T> error(String code, String message) {
        ErrorDetails error = new ErrorDetails(code, message, null);
        return new ApiResponse<>(false, null, null, error);
    }
    
    public static <T> ApiResponse<T> error(ErrorDetails error) {
        return new ApiResponse<>(false, null, null, error);
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetails {
        private String code;
        private String message;
        private Object details;
    }
}
