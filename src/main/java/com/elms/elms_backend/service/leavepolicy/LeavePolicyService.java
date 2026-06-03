package com.elms.elms_backend.service.leavepolicy;

import com.elms.elms_backend.dto.leavepolicy.CreateLeavePolicyDTO;
import com.elms.elms_backend.dto.leavepolicy.CreateLeavePolicyResponseDTO;
import com.elms.elms_backend.dto.leavepolicy.LeavePolicyProjectionDTO;
import com.elms.elms_backend.entity.LeavePolicyEntity;
import com.elms.elms_backend.entity.LeaveTypeEntity;


import java.time.LocalDate;
import java.util.List;


public interface LeavePolicyService {
    public List<LeavePolicyProjectionDTO> getCurrentActiveLeavePolicies();
    public List<LeavePolicyProjectionDTO> getLeavePolicies(Integer year);
    public CreateLeavePolicyResponseDTO createLeavePolicy(CreateLeavePolicyDTO createLeavePolicyDTO);
    public LeavePolicyEntity findLeavePolicyOrThrow(
            LeaveTypeEntity leaveType,
             Integer year
    );

    Integer calculateLeaveDays(LocalDate startDate, LocalDate endDate);
}
