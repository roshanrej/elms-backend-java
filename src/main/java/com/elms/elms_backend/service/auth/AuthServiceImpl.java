package com.elms.elms_backend.service.auth;

import com.elms.elms_backend.dto.auth.LoginRequestDTO;
import com.elms.elms_backend.dto.auth.LoginResponseDTO;
import com.elms.elms_backend.dto.auth.LogoutRequestDTO;
import com.elms.elms_backend.dto.user.UserContextDTO;
import com.elms.elms_backend.entity.DepartmentEntity;
import com.elms.elms_backend.entity.RefreshTokenEntity;
import com.elms.elms_backend.entity.RoleEntity;
import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.entity.enums.RoleEnum;
import com.elms.elms_backend.entity.enums.UserStatusEnum;
import com.elms.elms_backend.repository.auth.RefreshTokenRepository;
import com.elms.elms_backend.repository.department.DepartmentRepository;
import com.elms.elms_backend.repository.user.RoleRepository;
import com.elms.elms_backend.repository.user.UserRepository;
import com.elms.elms_backend.security.JwtService;
import com.elms.elms_backend.security.UserPrincipal;
import com.elms.elms_backend.service.user.UserService;

import org.springframework.boot.webmvc.autoconfigure.WebMvcProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final RefreshTokenRepository refreshTokenRepo;

    public AuthServiceImpl(
            RefreshTokenService refreshTokenService,
            DepartmentRepository deptRepo,
            RoleRepository roleRepo,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            UserRepository userRepo,
            UserService userService, RefreshTokenRepository refreshTokenRepo
    ) {
        this.refreshTokenService = refreshTokenService;


        this.deptRepo = deptRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userRepo = userRepo;
        this.userService = userService;
        this.refreshTokenRepo = refreshTokenRepo;
    }

    @Override
    public UserContextDTO validateSession() {
        UserEntity user = userService.getAuthenticatedUser();
        String department = user.getDepartment().getName();
        return new UserContextDTO(user.getName(),
                user.getEmail(),
                user.getRole().getName(),
                department);
    }

    /**
     * Authenticates user credentials and generates
     * JWT access token on successful login.
     *
     * @param loginRequestDto login request payload
     * @return authenticated login response
     */
    @Transactional
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
        if(user.getStatus()!= UserStatusEnum.ACTIVE){
            throw new RuntimeException("Invalid user");
        }

        if (!passwordEncoder.matches(
                loginRequestDto.getPassword(),
                user.getPasswordHash()
        )
        ) {
            throw new RuntimeException(
                    "Invalid credentials"
            );
        }

        refreshTokenRepo
                .findByUser(user)
                .ifPresent(token -> {
                    refreshTokenRepo.delete(token);
                    refreshTokenRepo.flush();
                });

        String department = user.getDepartment() != null
                ? user.getDepartment().getName()
                : null;

        String accessToken =
                jwtService.generateAccessToken(
                        new UserPrincipal(user)
                );

        RefreshTokenEntity refreshTokenEntity =
                refreshTokenService.createRefreshToken(user);

        String refreshToken = refreshTokenEntity.getToken();
        UserContextDTO userContext = new UserContextDTO(
                user.getName(),
                user.getEmail(),
                user.getRole().getName(),
                department);
        return new LoginResponseDTO(
                userContext,
                accessToken,
                refreshToken
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
                    "Email already exists"
            );
        }

        String hashedPassword =
                passwordEncoder.encode(password);

        DepartmentEntity departmentEntity =
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
                .department(departmentEntity)
                .status(UserStatusEnum.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        userRepo.save(user);
    }

    @Override
    @Transactional
    public void logoutUser(LogoutRequestDTO refreshTokenDTO) {
        refreshTokenService.deleteRefreshToken(refreshTokenDTO.getRefreshToken());
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