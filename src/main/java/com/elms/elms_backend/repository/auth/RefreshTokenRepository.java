package com.elms.elms_backend.repository.auth;

import com.elms.elms_backend.entity.RefreshTokenEntity;

import com.elms.elms_backend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshTokenEntity, Long> {

    @Query("""
            SELECT rt FROM RefreshTokenEntity rt
            JOIN FETCH rt.user u
            JOIN FETCH u.role
            JOIN FETCH u.department
            LEFT JOIN FETCH u.team
            WHERE rt.token = :token
            """)
    Optional<RefreshTokenEntity> findByTokenWithUser(@Param("token") String token);

    Optional<RefreshTokenEntity> findByToken(String token);
    Optional<RefreshTokenEntity> findByUser(UserEntity user);
    void deleteByUser(UserEntity user);
}