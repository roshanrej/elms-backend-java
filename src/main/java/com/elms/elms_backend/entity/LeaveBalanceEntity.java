package com.elms.elms_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Table(name= "leave_balances")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class LeaveBalanceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    UserEntity employee;
    @ManyToOne(optional = false)
    @JoinColumn(name = "leave_policy_id", nullable = false)
   LeavePolicyEntity leavePolicy;
    @Column(name = "consumed_leave", nullable = false)
    private Integer consumedLeave;
    @Column(name = "remaining_leave", nullable = false)
    private Integer remainingLeave;
    @Column(name ="updated_at")
    private LocalDateTime updatedAt;
}
