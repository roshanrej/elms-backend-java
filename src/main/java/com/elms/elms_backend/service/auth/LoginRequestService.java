package com.elms.elms_backend.service.auth;

import com.elms.elms_backend.dto.api.ApiResponseDTO;
import com.elms.elms_backend.dto.auth.LoginRequestDTO;
import com.elms.elms_backend.dto.auth.LoginResponseDTO;

public interface LoginRequestService  {
    public ApiResponseDTO<LoginResponseDTO> loginUser(LoginRequestDTO loginRequestDto);
    public void dummyRegister (  String username, String email, String password, String dept,  String role);
}
