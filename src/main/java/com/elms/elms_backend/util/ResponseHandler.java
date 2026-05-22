package com.elms.elms_backend.util;

import com.elms.elms_backend.dto.api.ApiResponseDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Utility class responsible for generating
 * standardized API responses across the application.
 */
public class ResponseHandler {

    /**
     * Generates successful API response.
     *
     * @param data response payload
     * @param message success message
     * @param status HTTP status
     * @param <T> response payload type
     * @return standardized success response entity
     */
    public static <T> ResponseEntity<ApiResponseDTO<?>>
    success(
            T data,
            String message,
            HttpStatus status
    ) {

        ApiResponseDTO<T> response =
                new ApiResponseDTO<>(
                        true,
                        data,
                        message
                );

        return new ResponseEntity<>(
                response,
                status
        );
    }

    /**
     * Generates failed API response.
     *
     * @param message failure message
     * @param status HTTP status
     * @return standardized failure response entity
     */
    public static ResponseEntity<ApiResponseDTO<?>>
    failure(
            String message,
            HttpStatus status
    ) {

        ApiResponseDTO<?> response =
                new ApiResponseDTO<>(
                        false,
                        null,
                        message
                );

        return new ResponseEntity<>(
                response,
                status
        );
    }
}