package com.elms.elms_backend.entity;

import com.elms.elms_backend.entity.enums.LeaveRequestStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "leave_requests",
        indexes = {
                @Index(name = "idx_leave_request_employee", columnList = "employee_id"),
                @Index(name = "idx_leave_request_status", columnList = "status"),
                @Index(name = "idx_leave_request_employee_status", columnList = "employee_id,status"),
                @Index(name = "idx_leave_request_year", columnList = "year")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private UserEntity employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id")
    private LeaveTypeEntity leaveType;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "no_of_days")
    private Integer noOfDays;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private LeaveRequestStatusEnum status;

    @Column(name = "year")
    private Integer year;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Version
    private Integer version;
}