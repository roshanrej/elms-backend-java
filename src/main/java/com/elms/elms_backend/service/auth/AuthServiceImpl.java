package com.elms.elms_backend.service.auth;

import com.elms.elms_backend.dto.auth.LoginRequestDTO;
import com.elms.elms_backend.dto.auth.LoginResponseDTO;
import com.elms.elms_backend.dto.auth.LogoutRequestDTO;
import com.elms.elms_backend.entity.Department;
import com.elms.elms_backend.entity.RefreshTokenEntity;
import com.elms.elms_backend.entity.RoleEntity;
import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.entity.enums.RoleEnum;
import com.elms.elms_backend.entity.enums.UserStatusEnum;
import com.elms.elms_backend.repository.department.DepartmentRepository;
import com.elms.elms_backend.repository.user.RoleRepository;
import com.elms.elms_backend.repository.user.UserRepository;
import com.elms.elms_backend.security.JwtService;
import com.elms.elms_backend.security.UserPrincipal;
import com.elms.elms_backend.service.user.UserService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service implementation responsible for:
 * - authentication workflows
 * - JWT token generation
 * - user registration operations
 */
@Service
public class AuthServiceImpl
        implements AuthService {

    private final RefreshTokenService refreshTokenService;
    private final DepartmentRepository deptRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserRepository userRepo;
    private final UserService userService;

    public AuthServiceImpl(
            RefreshTokenService refreshTokenService,
            DepartmentRepository deptRepo,
            RoleRepository roleRepo,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            UserRepository userRepo,
            UserService userService
    ) {
        this.refreshTokenService = refreshTokenService;


        this.deptRepo = deptRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userRepo = userRepo;
        this.userService = userService;
    }

    @Override
    public LoginResponseDTO validateSession() {
        UserEntity user = userService.getAuthenticatedUser();
        String department = user.getDepartment().getName();

        return new LoginResponseDTO(
                user.getName(),
                user.getEmail(),
                user.getRole().getName().toString(),
                department
        );
    }

    /**
     * Authenticates user credentials and generates
     * JWT access token on successful login.
     *
     * @param loginRequestDto login request payload
     * @return authenticated login response
     */
    @Override
    public LoginResponseDTO loginUser(
            LoginRequestDTO loginRequestDto
    ) {

        validateLoginPayload(loginRequestDto);

        UserEntity user =
                userRepo.findByEmail(
                        loginRequestDto.getEmail()
                ).orElseThrow(() ->
                        new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(
                loginRequestDto.getPassword(),
                user.getPasswordHash()
        )) {

            throw new RuntimeException(
                    "Invalid credentials"
            );
        }

        String department = user.getDepartment() != null
                ? user.getDepartment().getName()
                : null;

        String accessToken =
                jwtService.generateAccessToken(
                        new UserPrincipal(user)
                );

        RefreshTokenEntity refreshToken =
                refreshTokenService.createRefreshToken(user);


        return new LoginResponseDTO(
                user.getName(),
                user.getEmail(),
                user.getRole().getName().toString(),
                department,
                accessToken,
                refreshToken.getToken()
        );
    }

    /**
     * Registers a new system user.
     *
     * @param username user name
     * @param email    user email
     * @param password raw password
     * @param deptName department name
     * @param roleName role name
     */
    @Override
    public void dummyRegister(
            String username,
            String email,
            String password,
            String deptName,
            String roleName
    ) {

        validateRegistrationPayload(
                username,
                email,
                password,
                deptName,
                roleName
        );

        if (userRepo.findByEmail(email).isPresent()) {

            throw new IllegalStateException(
                    "Invalid credentials"
            );
        }

        String hashedPassword =
                passwordEncoder.encode(password);

        Department department =
                deptRepo.findByName(deptName)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid department"
                                )
                        );

        RoleEnum roleEnum;

        try {
            roleEnum = RoleEnum.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role");
        }

        RoleEntity role =
                roleRepo.findByName(roleEnum)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid role"
                                )
                        );

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

    @Override
    public void logoutUser(LogoutRequestDTO refreshTokenDTO) {
          refreshTokenService.revokeRefreshToken(refreshTokenDTO.getRefreshToken());
    }

    /**
     * Validates login request payload.
     *
     * @param loginRequestDto login request payload
     */
    private void validateLoginPayload(
            LoginRequestDTO loginRequestDto
    ) {

        if (loginRequestDto.getEmail() == null
                || loginRequestDto.getPassword() == null) {

            throw new RuntimeException(
                    "Missing required fields"
            );
        }
    }

    /**
     * Validates registration request payload.
     *
     * @param username user name
     * @param email    user email
     * @param password raw password
     * @param deptName department name
     * @param roleName role name
     */
    private void validateRegistrationPayload(
            String username,
            String email,
            String password,
            String deptName,
            String roleName
    ) {

        if (username == null
                || email == null
                || password == null
                || deptName == null
                || roleName == null) {

            throw new RuntimeException(
                    "Missing required fields"
            );
        }
    }
}