package com.elms.elms_backend.service.auth;

import com.elms.elms_backend.dto.api.ApiResponseDTO;
import com.elms.elms_backend.dto.auth.LoginRequestDTO;
import com.elms.elms_backend.dto.auth.LoginResponseDTO;
import com.elms.elms_backend.entity.Department;
import com.elms.elms_backend.entity.RoleEntity;
import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.entity.enums.UserStatusEnum;
import com.elms.elms_backend.repository.department.DepartmentRepository;
import com.elms.elms_backend.repository.user.RoleRepository;
import com.elms.elms_backend.repository.user.UserRepository;
import com.elms.elms_backend.security.JwtService;
import com.elms.elms_backend.security.UserPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LoginRequestServiceImpl implements LoginRequestService {

    private final UserRepository userRepo;
    private final DepartmentRepository deptRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginRequestServiceImpl(UserRepository userRepo, DepartmentRepository deptRepo, RoleRepository roleRepo, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepo = userRepo;
        this.deptRepo = deptRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;

    }

    @Override
    public ApiResponseDTO<LoginResponseDTO> loginUser(LoginRequestDTO loginRequestDto) {
        if (loginRequestDto.getEmail() == null || loginRequestDto.getPassword() == null) {
            throw new RuntimeException("Missing required fields.");
        }
        UserEntity user = userRepo.findByEmail(loginRequestDto.getEmail()).orElseThrow(() ->
                new RuntimeException("User with this email does not exist."));


        if (passwordEncoder.matches(loginRequestDto.getPassword(), user.getPasswordHash())) {
            String token =
                    jwtService.generateToken(
                            new UserPrincipal(user)
                    );

            LoginResponseDTO loginResponseData =
                    new LoginResponseDTO(
                            user.getName(),
                            user.getEmail(),
                            user.getRole().getName(),
                            token
                    );
            return new ApiResponseDTO<LoginResponseDTO>(true, loginResponseData, null);
        }
        return new ApiResponseDTO<>(false, null, "Login failed. Try again.");


    }

    public void dummyRegister(String username, String email, String password, String deptName, String roleName) {
        if (username == null || email == null || roleName == null || deptName == null || password == null) {
            throw new RuntimeException("Missing required fields");
        }

        userRepo.findByEmail(email).ifPresent(user -> {
            throw new RuntimeException("User already exists");
        });

        String hashedPassword = passwordEncoder.encode(password);

        Department department = deptRepo.findByName(deptName).orElseThrow(() ->
                new RuntimeException("Department doesn't exist"));

        RoleEntity role = roleRepo.findByName(roleName).orElseThrow(() ->
                new RuntimeException("Role doesn't exist"));

        UserEntity user = UserEntity.builder()
                .name(username)
                .email(email)
                .passwordHash(hashedPassword)
                .role(role)
                .department(department)
                .status(UserStatusEnum.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
        userRepo.save(user);
    }
}
