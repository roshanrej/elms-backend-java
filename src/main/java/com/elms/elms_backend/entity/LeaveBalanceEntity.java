package com.elms.elms_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "leave_balances",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_leave_balance",
                columnNames = {"employee_id", "leave_policy_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private UserEntity employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leave_policy_id", nullable = false)
    private LeavePolicyEntity leavePolicy;

    @Column(name = "consumed_leave", nullable = false)
    private Integer consumedLeave;

    @Column(name = "remaining_leave", nullable = false)
    private Integer remainingLeave;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}