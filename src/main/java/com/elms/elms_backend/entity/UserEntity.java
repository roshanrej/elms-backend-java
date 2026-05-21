package com.elms.elms_backend.entity;

import com.elms.elms_backend.entity.enums.UserStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(name="name")
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    // 🔹 RELATIONSHIPS

    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private DepartmentEntity departmentEntity;

    @Column(name="status")
    @Enumerated(EnumType.STRING)
    private UserStatusEnum status;

    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name="updated_at")
    private LocalDateTime updatedAt;
}
