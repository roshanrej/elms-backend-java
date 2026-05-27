package com.elms.elms_backend.entity;

import com.elms.elms_backend.entity.enums.UserStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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

    @Column(name="name", nullable = false)
    private String name;

    @Column(name="email",unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = true)
    private UserEntity manager;

    @OneToMany(mappedBy = "manager")
    private List<UserEntity> subordinates;
    // 🔹 RELATIONSHIPS

    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private DepartmentEntity department;

    @Column(name="status", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatusEnum status;

    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;
}
