package com.elms.elms_backend.service.leavetype;

import com.elms.elms_backend.entity.LeaveTypeEntity;
import com.elms.elms_backend.entity.enums.LeaveTypeStatusEnum;
import com.elms.elms_backend.repository.leave.LeaveTypeRepository;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service implementation responsible for
 * leave type resolution and validation logic.
 * <p>
 * This service acts as the domain boundary between:
 * - leave request workflow orchestration
 * - leave type persistence access
 */
@Service
public class LeaveTypeServiceImpl implements LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepo;

    public LeaveTypeServiceImpl(
            LeaveTypeRepository leaveTypeRepo
    ) {
        this.leaveTypeRepo = leaveTypeRepo;
    }

    /**
     * Resolves leave type if present.
     * <p>
     * Draft leave requests are allowed to omit
     * leave type information temporarily.
     *
     * @param leaveTypeName leave type name
     * @return resolved leave type entity or null
     */
    @Override
    public LeaveTypeEntity resolveOptionalLeaveType(
            String leaveTypeName
    ) {

        if (leaveTypeName == null
                || leaveTypeName.isBlank()) {

            return null;
        }

        return getLeaveTypeByName(leaveTypeName);
    }

    /**
     * Resolves and validates leave type for
     * submission workflow operations.
     * <p>
     * Submitted leave requests must reference
     * an active leave type.
     *
     * @param leaveTypeName leave type name
     * @return validated active leave type entity
     */
    @Override
    public LeaveTypeEntity resolveLeaveType(
            String leaveTypeName
    ) {

        LeaveTypeEntity leaveType =
                getLeaveTypeByName(leaveTypeName);

        if (leaveType.getStatus()
                != LeaveTypeStatusEnum.ACTIVE) {

            throw new RuntimeException(
                    "Inactive leave type"
            );
        }

        return leaveType;
    }




    /**
     * Retrieves leave type entity by name.
     *
     * @param leaveTypeName leave type name
     * @return resolved leave type entity
     */
    private LeaveTypeEntity getLeaveTypeByName(
            String leaveTypeName
    ) {

        return leaveTypeRepo.findByName(leaveTypeName)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invalid leave type"
                        )
                );
    }
}