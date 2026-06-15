package com.elms.elms_backend.entity;

import com.elms.elms_backend.entity.enums.LeaveTypeStatusEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "leave_types",
        uniqueConstraints = @UniqueConstraint(name = "uq_leave_types_name", columnNames = "name")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true, nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LeaveTypeStatusEnum status;
}