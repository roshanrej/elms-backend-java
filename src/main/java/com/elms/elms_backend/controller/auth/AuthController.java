package com.elms.elms_backend.controller.auth;

import com.elms.elms_backend.dto.api.ApiResponseDTO;
import com.elms.elms_backend.dto.auth.LoginRequestDTO;
import com.elms.elms_backend.dto.auth.LoginResponseDTO;
import com.elms.elms_backend.dto.token.RefreshTokenRequestDTO;
import com.elms.elms_backend.entity.RefreshTokenEntity;
import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.security.JwtService;
import com.elms.elms_backend.security.UserPrincipal;
import com.elms.elms_backend.service.auth.LoginRequestService;
import com.elms.elms_backend.service.auth.RefreshTokenService;
import com.elms.elms_backend.util.ResponseHandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller responsible for:
 * - authentication endpoints
 * - login workflows
 * - registration workflows
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final LoginRequestService loginRequestService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    public AuthController(
            LoginRequestService loginRequestService, JwtService jwtService, RefreshTokenService refreshTokenService
    ) {
        this.loginRequestService = loginRequestService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Authenticates user credentials and returns
     * JWT token on successful login.
     *
     * @param loginRequest login request payload
     * @return standardized login response
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<?>>
    loginUser(
            @RequestBody LoginRequestDTO loginRequest
    ) {

        LoginResponseDTO response =
                loginRequestService.loginUser(loginRequest);

        return ResponseHandler.success(
                response,
                "Login successful",
                HttpStatus.OK
        );
    }

    /**
     * Refreshes expired access token using
     * valid refresh token issued during login.
     *
     * This endpoint validates refresh token state,
     * reconstructs authenticated user identity,
     * and generates a new short-lived access token.
     *
     * @param request refresh token request payload
     * @return standardized API response containing
     * newly generated access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseDTO<?>>
    refreshToken(
            @RequestBody
            RefreshTokenRequestDTO request
    ) {

        RefreshTokenEntity refreshToken =
                refreshTokenService.validateRefreshToken(
                        request.getRefreshToken()
                );

        UserEntity user = refreshToken.getUser();

        String newAccessToken =
                jwtService.generateAccessToken(
                        new UserPrincipal(user)
                );

        return ResponseHandler.success(
                newAccessToken,
                "Access token refreshed successfully",
                HttpStatus.OK
        );
    }

    /**
     * Registers a new user into the system.
     *
     * @param body registration payload
     * @return standardized registration response
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO<?>>
    dummyRegister(
            @RequestBody Map<String, String> body
    ) {

        try {

            loginRequestService.dummyRegister(
                    body.get("username"),
                    body.get("email"),
                    body.get("password"),
                    body.get("department"),
                    body.get("role")
            );

            return ResponseHandler.success(
                    null,
                    "User registered successfully",
                    HttpStatus.CREATED
            );

        } catch (RuntimeException e) {

            return ResponseHandler.failure(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}