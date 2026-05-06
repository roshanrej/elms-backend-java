package com.elms.elms_backend.entity;

import com.elms.elms_backend.entity.enums.LeaveActionEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name="leave_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "leave_id", nullable = false)
    private LeaveRequest leaveRequest;

    @Column(name = "action", nullable = false)
    @Enumerated(EnumType.STRING)
    private LeaveActionEnum action;

    @ManyToOne(optional = false)
    @JoinColumn(name="actor_id", nullable = false)
    private User actor;

    @ManyToOne(optional = false)
    @JoinColumn(name="actor_role_id", nullable = false)
    private Role actorRole;

    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt;

  @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json", name="metadata")
    private Map<String, Object> metadata;


}
