package com.elms.elms_backend.service.leavepolicy;

import com.elms.elms_backend.dto.leavepolicy.LeavePolicyProjectionDTO;
import com.elms.elms_backend.entity.enums.LeaveTypeStatusEnum;

import java.util.List;


public interface LeavePolicyService {
    public List<LeavePolicyProjectionDTO> getCurrentActiveLeavePolicies();
    public List<LeavePolicyProjectionDTO> getLeavePolicies(Integer year);
}
