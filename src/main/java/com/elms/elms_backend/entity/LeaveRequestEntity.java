package com.elms.elms_backend.entity;

import com.elms.elms_backend.entity.enums.LeaveRequestStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 MUST exist
    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private UserEntity employee;


    @ManyToOne(optional = true)
    @JoinColumn(name = "leave_type_id", nullable = true)
    private LeaveTypeEntity leaveType;

    // OPTIONAL

    @Column(name = "start_date", nullable = true)
    private LocalDate startDate;

    @Column(name="end_date", nullable = true)
    private LocalDate endDate;

    @Column(name = "no_of_days", nullable = true)
    private Integer noOfDays;

    @Column(name="reason",nullable = true)
    private String reason;

    @Enumerated(EnumType.STRING)
    private LeaveRequestStatusEnum status;

    @Column(name = "created_at",nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Version
    private Integer version;
}