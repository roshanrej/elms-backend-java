package com.elms.elms_backend.service.leavepolicy;

import com.elms.elms_backend.dto.leavepolicy.CreateLeavePolicyDTO;
import com.elms.elms_backend.dto.leavepolicy.CreateLeavePolicyResponseDTO;
import com.elms.elms_backend.dto.leavepolicy.LeavePolicyProjectionDTO;


import java.util.List;


public interface LeavePolicyService {
    public List<LeavePolicyProjectionDTO> getCurrentActiveLeavePolicies();
    public List<LeavePolicyProjectionDTO> getLeavePolicies(Integer year);
    public CreateLeavePolicyResponseDTO createLeavePolicy(CreateLeavePolicyDTO createLeavePolicyDTO);
}
