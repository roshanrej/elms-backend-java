package com.elms.elms_backend.service.leavepolicy;

import com.elms.elms_backend.dto.leavepolicy.LeavePolicyProjectionDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface LeavePolicyService {
    public Long computeTotalAllocatedLeave();
    public List<LeavePolicyProjectionDTO> getCurrentYearPolicies();
    public List<LeavePolicyProjectionDTO> getPoliciesByYear(Integer year);
}
