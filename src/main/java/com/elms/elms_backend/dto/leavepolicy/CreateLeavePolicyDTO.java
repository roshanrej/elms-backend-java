package com.elms.elms_backend.dto.leavepolicy;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateLeavePolicyDTO {
    private String leaveType;
    private Integer year;
    private Integer allocatedLeave;
    private Integer noticePeriodDays;
}
