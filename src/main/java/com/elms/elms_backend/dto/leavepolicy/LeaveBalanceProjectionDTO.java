package com.elms.elms_backend.dto.leavepolicy;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class LeaveBalanceProjectionDTO {
    private String leaveTypeName;
    private Integer year;
    private Integer allocatedLeave;
    private Integer consumedLeave;
    private Integer remainingLeave;
}
