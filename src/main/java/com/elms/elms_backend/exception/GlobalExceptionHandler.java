package com.elms.elms_backend.exception;


import com.elms.elms_backend.dto.api.ApiResponseDTO;
import com.elms.elms_backend.util.ResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponseDTO<?>> handleBusinessException(BusinessException ex) {
        return ResponseHandler.failure(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDTO<?>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseHandler.failure(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponseDTO<?>> handleIllegalStateException(IllegalStateException ex) {
        return ResponseHandler.failure(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponseDTO<?>>
    handleRuntimeException(RuntimeException ex) {
        return ResponseHandler.failure(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }
}