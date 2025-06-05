package com.codepipeline.mcp.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ValidationException extends BaseException {
    
    private final Map<String, String> validationErrors;
    
    public ValidationException(String message, Map<String, String> validationErrors) {
        super(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
        this.validationErrors = validationErrors;
    }
    
    public ValidationException(String message, Map<String, String> validationErrors, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, cause);
        this.validationErrors = validationErrors;
    }
    
    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
}
