package com.elms.elms_backend.entity;

import com.elms.elms_backend.entity.enums.LeaveTypeStatusEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="leave_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Integer id;

    @Column(name = "name",unique = true, nullable = false)
    private String name;

    @Column(name="status", nullable = false)
    @Enumerated(EnumType.STRING)
    private LeaveTypeStatusEnum status;




}
