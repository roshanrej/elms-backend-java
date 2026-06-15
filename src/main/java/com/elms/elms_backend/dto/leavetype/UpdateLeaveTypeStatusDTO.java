package com.elms.elms_backend.dto.leavetype;

import com.elms.elms_backend.entity.enums.LeaveTypeStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateLeaveTypeStatusDTO {
    private LeaveTypeStatusEnum status;
}
