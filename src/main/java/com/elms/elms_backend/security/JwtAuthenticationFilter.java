package com.elms.elms_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter responsible for:
 * - extracting JWT tokens from requests
 * - validating token authenticity
 * - reconstructing authenticated user identity
 * - populating Spring SecurityContext
 *
 * This filter executes once per request
 * before protected endpoint authorization.
 */
@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService customUserDetailsService
    ) {

        this.jwtService = jwtService;
        this.customUserDetailsService =
                customUserDetailsService;
    }

    /**
     * Intercepts incoming requests and performs
     * JWT-based authentication workflow.
     *
     * Workflow:
     * - extract Authorization header
     * - validate Bearer token presence
     * - extract email from JWT
     * - load user details
     * - validate token authenticity
     * - reconstruct Authentication object
     * - populate SecurityContext
     *
     * @param request incoming HTTP request
     * @param response outgoing HTTP response
     * @param filterChain remaining filter chain
     * @throws ServletException servlet exception
     * @throws IOException I/O exception
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader =
                request.getHeader("Authorization");

        final String jwtToken;
        final String email;

        /**
         * Skip authentication workflow if
         * Authorization header is absent
         * or does not contain Bearer token.
         */
        if (authHeader == null
                || !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        /**
         * Extract JWT token from:
         * "Bearer <token>"
         */
        jwtToken = authHeader.substring(7);

        /**
         * Extract authenticated user identity
         * from JWT payload.
         */
        email =
                jwtService.extractUsername(jwtToken);

        System.out.println("JWT FILTER HIT");
        System.out.println("EMAIL: " + email);
        System.out.println("AUTH-HEADER: " + authHeader);

        /**
         * Prevent duplicate authentication
         * reconstruction if context already exists.
         */
        if (email != null
                && SecurityContextHolder
                .getContext()
                .getAuthentication() == null) {

            /**
             * Reconstruct authenticated user details
             * from persistence layer.
             */
            UserDetails userDetails =
                    customUserDetailsService
                            .loadUserByUsername(email);

            /**
             * Validate token ownership and expiry.
             */
            if (jwtService.isTokenValid(
                    jwtToken,
                    userDetails
            )) {

                /**
                 * Construct authenticated identity object
                 * recognized by Spring Security.
                 */
                UsernamePasswordAuthenticationToken
                        authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                /**
                 * Attach request-specific metadata
                 * to authentication context.
                 */
                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                /**
                 * Populate SecurityContext with
                 * reconstructed authenticated identity.
                 */
                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authToken);
            }
        }

        /**
         * Continue remaining request lifecycle.
         */
        filterChain.doFilter(
                request,
                response
        );
    }
}