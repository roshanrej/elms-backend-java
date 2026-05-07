package com.elms.elms_backend.dto.api;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponseDTO<T> {
    private boolean success;
    private T data;
    private String error;
}
