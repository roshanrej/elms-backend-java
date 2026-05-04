package com.elms.elms_backend.entity;

import com.elms.elms_backend.entity.enums.UserActionEnum;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name="user_audit_logs")
public class UserAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne()
    @JoinColumn(name="user_id")
    private  User user;

    @Enumerated(EnumType.STRING)
    @Column(name="action")
    private UserActionEnum action;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json", name="metadata")
    private Map<String, Object> metadata;

}
