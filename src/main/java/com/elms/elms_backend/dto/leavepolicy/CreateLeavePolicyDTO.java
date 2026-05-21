package com.elms.elms_backend.dto.leavepolicy;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateLeavePolicyDTO {
    String leaveType;
    Integer year;
    Integer allocatedLeave;

}
