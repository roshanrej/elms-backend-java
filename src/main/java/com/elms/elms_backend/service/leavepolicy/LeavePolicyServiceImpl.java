package com.elms.elms_backend.service.leavepolicy;


import com.elms.elms_backend.dto.leavepolicy.LeavePolicyProjectionDTO;
import com.elms.elms_backend.repository.leave.LeavePolicyRepository;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.Year;
import java.util.List;

public class LeavePolicyServiceImpl implements LeavePolicyService {
    private final LeavePolicyRepository leavePolicyRepo;

    public LeavePolicyServiceImpl(LeavePolicyRepository leavePolicyRepo) {
        this.leavePolicyRepo = leavePolicyRepo;
    }

    @Override
    public Long computeTotalAllocatedLeave() {
//        Long totalAllocatedLeave = leavePolicyRepo.
       return 0L;
    }


    @Override
    public List<LeavePolicyProjectionDTO> getCurrentYearPolicies() {
        Integer year = Year.now().getValue();
        return leavePolicyRepo.findPoliciesByYear(year);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public List<LeavePolicyProjectionDTO> getPoliciesByYear(Integer year) {
        return leavePolicyRepo.findPoliciesByYear(year);
    }
}
