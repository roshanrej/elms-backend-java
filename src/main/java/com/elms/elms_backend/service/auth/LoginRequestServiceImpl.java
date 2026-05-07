package com.elms.elms_backend.service.auth;

import com.elms.elms_backend.dto.api.ApiResponseDTO;
import com.elms.elms_backend.dto.auth.LoginRequestDTO;
import com.elms.elms_backend.dto.auth.LoginResponseDTO;
import com.elms.elms_backend.entity.User;
import com.elms.elms_backend.repository.user.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginRequestServiceImpl implements LoginRequestService {

    private final UserRepository userRepo;

    public LoginRequestServiceImpl(UserRepository userRepo){
        this.userRepo = userRepo;

    }

    @Override
    public ApiResponseDTO<LoginResponseDTO> loginUser(LoginRequestDTO loginRequestDto){
        if(loginRequestDto.getEmail() == null || loginRequestDto.getPassword() == null){
            throw new RuntimeException("Missing required fields.");
        }
        User user = userRepo.findByEmail(loginRequestDto.getEmail()).orElseThrow(() ->
                new RuntimeException("User with this email does not exist."));

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

        if(encoder.matches(loginRequestDto.getPassword(), user.getPasswordHash())){
            //jwt token generation later

            LoginResponseDTO loginResponseData = new LoginResponseDTO(user.getName(),user.getEmail(),user.getRole().getName());
            return new ApiResponseDTO<LoginResponseDTO>(true,loginResponseData,null );
        }
        return new ApiResponseDTO<>(false,null,"Login failed. Try again." );


    }
}
