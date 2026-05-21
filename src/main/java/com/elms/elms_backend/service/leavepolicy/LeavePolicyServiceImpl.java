package com.elms.elms_backend.service.leavepolicy;


import com.elms.elms_backend.dto.leavepolicy.CreateLeavePolicyDTO;
import com.elms.elms_backend.dto.leavepolicy.CreateLeavePolicyResponseDTO;
import com.elms.elms_backend.dto.leavepolicy.LeavePolicyProjectionDTO;
import com.elms.elms_backend.entity.LeavePolicyEntity;
import com.elms.elms_backend.entity.LeaveTypeEntity;
import com.elms.elms_backend.entity.enums.LeaveTypeStatusEnum;
import com.elms.elms_backend.mapper.leave.LeavePolicyMapper;
import com.elms.elms_backend.repository.leave.LeavePolicyRepository;
import com.elms.elms_backend.repository.leave.LeaveTypeRepository;
import com.elms.elms_backend.service.leavetype.LeaveTypeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;
@Service
public class LeavePolicyServiceImpl implements LeavePolicyService {
    private final LeavePolicyRepository leavePolicyRepo;
    private final LeaveTypeService leaveTypeService;
    private final LeavePolicyMapper leavePolicyMapper;

    public LeavePolicyServiceImpl(LeavePolicyRepository leavePolicyRepo, LeaveTypeService leaveTypeService, LeavePolicyMapper leavePolicyMapper) {
        this.leavePolicyRepo = leavePolicyRepo;

        this.leaveTypeService = leaveTypeService;
        this.leavePolicyMapper = leavePolicyMapper;
    }


    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
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

    /**
     * @param createLeavePolicyDTO
     * @return leave policy projection on specific leave Type, year
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    @Transactional
    public CreateLeavePolicyResponseDTO createLeavePolicy(CreateLeavePolicyDTO createLeavePolicyDTO) {
        Integer year = createLeavePolicyDTO.getYear();
        Integer allocatedLeave = createLeavePolicyDTO.getAllocatedLeave();
        LeaveTypeEntity leaveTypeEntity = leaveTypeService.resolveLeaveType(createLeavePolicyDTO.getLeaveType()); // check for valid leave Type
        if (leavePolicyRepo.existsByLeaveTypeAndYear(leaveTypeEntity, year)) {
            throw new IllegalStateException("Leave policy already exists");
        }
        if(allocatedLeave <= 0){
            throw new IllegalArgumentException("Allocated leave must be greater than zero");
        }
        LeavePolicyEntity leavePolicyEntity = LeavePolicyEntity.builder()
                .leaveType( leaveTypeEntity)
                .year(year)
                .allocatedLeave(allocatedLeave)
                .build();
        LeavePolicyEntity savedLeavePolicy = leavePolicyRepo.save(leavePolicyEntity);


       return leavePolicyMapper.mapToResponse(savedLeavePolicy);
    }
}
