package com.elms.elms_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "leave_policies",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_leave_policy",
                columnNames = {"year", "leave_type_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeavePolicyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveTypeEntity leaveType;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "allocated_leave", nullable = false)
    private Integer allocatedLeave;

    @Column(name = "notice_period_days", nullable = false)
    private Integer noticePeriodDays;
}