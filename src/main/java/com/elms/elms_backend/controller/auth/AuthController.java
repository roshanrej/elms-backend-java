package com.elms.elms_backend.controller.auth;

import com.elms.elms_backend.dto.api.ApiResponseDTO;
import com.elms.elms_backend.dto.auth.LoginRequestDTO;
import com.elms.elms_backend.dto.auth.LoginResponseDTO;
import com.elms.elms_backend.service.auth.LoginRequestService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
     private final LoginRequestService loginRequestService;
     public AuthController(LoginRequestService loginRequestService){
         this.loginRequestService = loginRequestService;
     }
    @PostMapping("/login")
    public ApiResponseDTO<LoginResponseDTO> loginUser(@RequestBody  LoginRequestDTO loginRequest){
     try{
         return loginRequestService.loginUser(loginRequest);

     }
     catch (RuntimeException e){
return new ApiResponseDTO<> (false, null, e.getMessage());
     }

    }
}
