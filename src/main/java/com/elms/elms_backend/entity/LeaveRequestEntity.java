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
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    // 🔹 MUST exist
    @ManyToOne(optional = false)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveTypeEntity leaveType;

    // 🔹 OPTIONAL
    @ManyToOne
    @JoinColumn(name = "approver_id")
    private UserEntity approver;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name="end_date")
    private LocalDate endDate;

    @Column(name="reason")
    private String reason;

    @Enumerated(EnumType.STRING)
    private LeaveRequestStatusEnum status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    @Column(name = "decision_at")
    private LocalDateTime decisionAt;

    @Column(name="year")
    private Integer year;

}