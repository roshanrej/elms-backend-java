package com.elms.elms_backend.controller.auth;

import com.elms.elms_backend.dto.api.ApiResponseDTO;
import com.elms.elms_backend.dto.auth.LoginRequestDTO;
import com.elms.elms_backend.dto.auth.LoginResponseDTO;
import com.elms.elms_backend.dto.auth.LogoutRequestDTO;
import com.elms.elms_backend.dto.token.AccessTokenRequestDTO;
import com.elms.elms_backend.dto.token.AccessTokenResponseDTO;
import com.elms.elms_backend.dto.user.UserContextDTO;
import com.elms.elms_backend.entity.RefreshTokenEntity;
import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.security.JwtService;
import com.elms.elms_backend.security.UserPrincipal;
import com.elms.elms_backend.service.auth.AuthService;
import com.elms.elms_backend.service.auth.RefreshTokenService;
import com.elms.elms_backend.util.ResponseHandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsible for:
 * - authentication endpoints
 * - login workflows
 * - registration workflows
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(
            AuthService authService, JwtService jwtService, RefreshTokenService refreshTokenService
    ) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDTO<?>> logoutUser(@RequestBody LogoutRequestDTO logoutRequestDTO) {
        authService.logoutUser(logoutRequestDTO);
        return ResponseHandler.success(
                null,
                "User logged out successfully",
                HttpStatus.OK
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponseDTO<?>> validateSession() {

        UserContextDTO response =
                authService.validateSession();

        return ResponseHandler.success(
                response,
                "Session valid",
                HttpStatus.OK
        );
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
                authService.loginUser(loginRequest);

        return ResponseHandler.success(
                response,
                "Login successful",
                HttpStatus.OK
        );
    }

    /**
     * Refreshes expired access token using
     * valid refresh token issued during login.
     * <p>
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
            AccessTokenRequestDTO request
    ) {

        RefreshTokenEntity refreshToken =
                refreshTokenService.validateRefreshToken(
                        request.getRefreshToken()
                );

        UserEntity user = refreshToken.getUser();
        UserPrincipal userPrincipal = new UserPrincipal(user);
        String accessToken = jwtService.generateAccessToken(userPrincipal);
        UserContextDTO userContextDTO = new UserContextDTO(user.getName(),user.getEmail(), user.getRole().getName(), user.getDepartment().getName());

        AccessTokenResponseDTO accessTokenResponseDTO = new AccessTokenResponseDTO(accessToken,userContextDTO);

        return ResponseHandler.success(
                accessTokenResponseDTO,
                "Access token refreshed successfully",
                HttpStatus.OK
        );
    }


}