package com.elms.elms_backend.service.leavepolicy;


import com.elms.elms_backend.dto.leavepolicy.LeavePolicyProjectionDTO;
import com.elms.elms_backend.entity.enums.LeaveTypeStatusEnum;
import com.elms.elms_backend.repository.leave.LeavePolicyRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.List;
@Service
public class LeavePolicyServiceImpl implements LeavePolicyService {
    private final LeavePolicyRepository leavePolicyRepo;

    public LeavePolicyServiceImpl(LeavePolicyRepository leavePolicyRepo) {
        this.leavePolicyRepo = leavePolicyRepo;
    }


    @PreAuthorize("hasRole('EMPLOYEE')")
    @Override
    public List<LeavePolicyProjectionDTO> getCurrentActiveLeavePolicies() {
        Integer year = Year.now().getValue();
        return leavePolicyRepo.findPoliciesByYearAndStatus(year, LeaveTypeStatusEnum.ACTIVE);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public List<LeavePolicyProjectionDTO> getLeavePolicies(Integer year) {
        return leavePolicyRepo.findPoliciesByYear(year);
    }
}
