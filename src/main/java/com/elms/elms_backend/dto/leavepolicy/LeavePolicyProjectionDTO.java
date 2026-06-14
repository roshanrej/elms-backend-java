package com.elms.elms_backend.dto.leavepolicy;
import com.elms.elms_backend.entity.enums.LeaveTypeStatusEnum;
import lombok.*;
import org.hibernate.annotations.AnyKeyJavaClass;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LeavePolicyProjectionDTO {
    private String leaveTypeName;
    private LeaveTypeStatusEnum status;
    private Integer allocatedLeave;
    private Integer year;
    private Integer noticePeriodDays;
}
