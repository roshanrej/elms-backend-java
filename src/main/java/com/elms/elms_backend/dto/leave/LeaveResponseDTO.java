package com.elms.elms_backend.dto.leave;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class LeaveResponseDTO {
    private Integer id;
    private Integer leaveTypeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String status;
    private Integer year;
}
