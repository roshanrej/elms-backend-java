package com.elms.elms_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Year;

@Entity
@Table(name="leave_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeavePolicyEntity {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveTypeEntity leaveType;

    @Column(name="year", nullable = false)
    private Integer year;

    @Column(name="allocated_leave", nullable = false)
    private Integer allocatedLeave;
    @Column(name = "notice_period_days", nullable = false)
    private Integer noticePeriodDays;

}
