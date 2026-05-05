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
public class LeavePolicy {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "leave_type_id")
    private LeaveType leaveType;

    @Column(name="year")
    private Year year;

    @Column(name="allocated_leave")
    private int allocatedLeave;


}
