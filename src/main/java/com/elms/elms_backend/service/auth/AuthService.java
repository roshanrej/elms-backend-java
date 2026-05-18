package com.elms.elms_backend.service.auth;

import com.elms.elms_backend.dto.auth.LoginRequestDTO;
import com.elms.elms_backend.dto.auth.LoginResponseDTO;
import com.elms.elms_backend.dto.auth.LogoutRequestDTO;

public interface AuthService {
    public LoginResponseDTO validateSession();
    public LoginResponseDTO loginUser(LoginRequestDTO loginRequestDto);
    public void dummyRegister (  String username, String email, String password, String dept,  String role);
    public void logoutUser(LogoutRequestDTO logoutRequestDTO);
}
