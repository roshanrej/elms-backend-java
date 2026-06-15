package com.elms.elms_backend.service.leavetype;

import com.elms.elms_backend.dto.leavetype.*;
import com.elms.elms_backend.entity.LeaveTypeEntity;
import com.elms.elms_backend.entity.enums.LeaveTypeStatusEnum;
import com.elms.elms_backend.repository.leave.LeaveTypeRepository;

import org.springframework.security.access.prepost.PreAuthorize;
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

    public LeaveTypeServiceImpl(LeaveTypeRepository leaveTypeRepo) {
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
    public LeaveTypeEntity resolveOptionalLeaveType(String leaveTypeName) {
        if (leaveTypeName == null || leaveTypeName.isBlank()) {
            return null;
        }
        return getLeaveTypeByName(leaveTypeName);
    }

    /**
     * Resolves and validates leave type for
     * submission workflow operations.
     * Submitted leave requests must reference
     * an active leave type.
     *
     * @param leaveTypeName leave type name
     * @return validated active leave type entity
     * @throws IllegalStateException if the leave type is inactive
     */
    @Override
    public LeaveTypeEntity resolveLeaveType(String leaveTypeName) {
        LeaveTypeEntity leaveType = getLeaveTypeByName(leaveTypeName);

        if (leaveType.getStatus() != LeaveTypeStatusEnum.ACTIVE) {
            throw new IllegalStateException("Inactive leave type.");
        }

        return leaveType;
    }

    /**
     * Creates a new leave type in the system.
     *
     * @param createLeaveTypeDTO data transfer object containing the new leave type name
     * @return a projection of the newly created leave type
     * @throws IllegalArgumentException if the name is blank
     * @throws IllegalStateException if a leave type with the same name already exists
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public CreateLeaveTypeResponseDTO createLeaveType(CreateLeaveTypeDTO createLeaveTypeDTO) {
        if (createLeaveTypeDTO.getName() == null || createLeaveTypeDTO.getName().isBlank()) {
            throw new IllegalArgumentException("Leave name cannot be empty.");
        }

        if (leaveTypeRepo.existsByName(createLeaveTypeDTO.getName())) {
            throw new IllegalStateException("Leave type already exists.");
        }

        LeaveTypeEntity leaveType = LeaveTypeEntity.builder()
                .status(LeaveTypeStatusEnum.ACTIVE)
                .name(createLeaveTypeDTO.getName())
                .build();

        LeaveTypeEntity savedLeaveType = leaveTypeRepo.save(leaveType);

        return new CreateLeaveTypeResponseDTO(
                savedLeaveType.getId(),
                savedLeaveType.getName(),
                savedLeaveType.getStatus()
        );
    }

    /**
     * Retrieves leave type entity by name.
     *
     * @param leaveTypeName leave type name
     * @return resolved leave type entity
     * @throws IllegalArgumentException if the leave type does not exist
     */
    private LeaveTypeEntity getLeaveTypeByName(String leaveTypeName) {
        return leaveTypeRepo.findByName(leaveTypeName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid leave type."));
    }

    /**
     * Retrieves a list of all active leave type names.
     * Useful for populating dropdowns in the UI for leave application.
     *
     * @return list of active leave type names
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public List<String> getActiveLeaveTypes() {
        return leaveTypeRepo.findByStatusIn(List.of(LeaveTypeStatusEnum.ACTIVE))
                .stream()
                .map(LeaveTypeEntity::getName)
                .toList();
    }

    /**
     * Retrieves a comprehensive list of all leave types (both active and inactive).
     *
     * @return list of leave type projections for admin management
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public List<LeaveTypeProjectionDTO> getLeaveTypes() {
        return leaveTypeRepo.findAll()
                .stream()
                .map(lr -> new LeaveTypeProjectionDTO(lr.getId(), lr.getName(), lr.getStatus()))
                .toList();
    }

    /**
     * Updates the status (ACTIVE/INACTIVE) of an existing leave type.
     *
     * @param id the ID of the leave type to update
     * @param dto data transfer object containing the new status
     * @return a projection of the updated leave type
     * @throws IllegalArgumentException if the ID is invalid or status is null
     * @throws IllegalStateException if the leave type is already in the requested status
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public LeaveTypeProjectionDTO updateStatus(Long id, UpdateLeaveTypeStatusDTO dto) {
        LeaveTypeEntity leaveType = leaveTypeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid leave type."));

        if (dto.getStatus() == null) {
            throw new IllegalArgumentException("Invalid leave type status.");
        }

        if (leaveType.getStatus().equals(dto.getStatus())) {
            throw new IllegalStateException("Leave type is already " + dto.getStatus() + ".");
        }

        leaveType.setStatus(dto.getStatus());
        LeaveTypeEntity savedLeaveType = leaveTypeRepo.save(leaveType);

        return new LeaveTypeProjectionDTO(
                savedLeaveType.getId(),
                savedLeaveType.getName(),
                savedLeaveType.getStatus()
        );
    }

    /**
     * Renames an existing leave type.
     *
     * @param id the ID of the leave type to update
     * @param dto data transfer object containing the new name
     * @return a projection of the updated leave type
     * @throws IllegalArgumentException if the ID is invalid or the new name is blank
     * @throws IllegalStateException if the new name is already in use by another leave type
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public LeaveTypeProjectionDTO updateName(Long id, RenameLeaveTypeDTO dto) {
        LeaveTypeEntity leaveType = leaveTypeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid leave type."));

        if (dto.getLeaveTypeName() == null || dto.getLeaveTypeName().isBlank()) {
            throw new IllegalArgumentException("Leave type name cannot be blank.");
        }

        // Prevent renaming to an already existing leave type name
        if (!leaveType.getName().equalsIgnoreCase(dto.getLeaveTypeName()) &&
                leaveTypeRepo.existsByName(dto.getLeaveTypeName())) {
            throw new IllegalStateException("A leave type with this name already exists.");
        }

        leaveType.setName(dto.getLeaveTypeName());
        LeaveTypeEntity savedLeaveType = leaveTypeRepo.save(leaveType);

        return new LeaveTypeProjectionDTO(
                savedLeaveType.getId(),
                savedLeaveType.getName(),
                savedLeaveType.getStatus()
        );
    }
}