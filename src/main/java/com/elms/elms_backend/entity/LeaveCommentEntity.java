package com.elms.elms_backend.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name="leave_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveCommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   @ManyToOne
    @JoinColumn(name="leave_id",  nullable = false)
    private LeaveRequestEntity leaveRequest;

   @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private UserEntity user;

   @Column(name="message", nullable = false)
    private String message;

   @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt;
}
