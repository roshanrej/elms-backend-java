package com.elms.elms_backend.dto.leave;

import com.elms.elms_backend.entity.enums.LeaveActionEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;

@Data
public class LeaveRequestDTO {

    private Long userId;
    private String leaveType;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    private String reason;
    private LeaveActionEnum action;

}