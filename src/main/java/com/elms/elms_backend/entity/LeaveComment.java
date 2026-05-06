package com.elms.elms_backend.entity;


import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name="leave_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   @ManyToOne
    @JoinColumn(name="leave_id")
    private  LeaveRequest leaveRequest;

   @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

   @Column(name="message", nullable = false)
    private String message;

   @Column(name="created_at")
    private LocalDateTime createdAt;
}
