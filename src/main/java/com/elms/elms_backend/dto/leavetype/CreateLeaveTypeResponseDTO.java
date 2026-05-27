package com.elms.elms_backend.dto.leavetype;

import com.elms.elms_backend.entity.enums.LeaveTypeStatusEnum;
import lombok.*;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeaveTypeResponseDTO {
    private Long id;
    private String name;
    private LeaveTypeStatusEnum status;
}
