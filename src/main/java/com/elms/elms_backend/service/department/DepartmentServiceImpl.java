package com.elms.elms_backend.service.department;

import com.elms.elms_backend.dto.department.DepartmentProjectionDTO;
import com.elms.elms_backend.entity.DepartmentEntity;
import com.elms.elms_backend.entity.enums.DepartmentStatusEnum;
import com.elms.elms_backend.exception.BusinessException;
import com.elms.elms_backend.repository.department.DepartmentRepository;
import com.elms.elms_backend.repository.user.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public DepartmentServiceImpl(
            DepartmentRepository departmentRepository,
            UserRepository userRepository
    ) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Override
    @Transactional(readOnly = true)
    public List<DepartmentProjectionDTO> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::toProjection)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    @Transactional
    public DepartmentProjectionDTO createDepartment(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("Department name is required");
        }

        String normalizedName = name.strip().toUpperCase();
        if (departmentRepository.findByName(normalizedName).isPresent()) {
            throw new BusinessException("Department already exists");
        }

        DepartmentEntity department = DepartmentEntity.builder()
                .name(normalizedName)
                .status(DepartmentStatusEnum.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        return toProjection(departmentRepository.save(department));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    @Transactional
    public DepartmentProjectionDTO updateStatus(Long departmentId, DepartmentStatusEnum status) {
        if (status == null) {
            throw new BusinessException("Invalid department status");
        }

        DepartmentEntity department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BusinessException("Department not found"));

        if (status == DepartmentStatusEnum.INACTIVE
                && userRepository.countByDepartmentId(departmentId) > 0) {
            throw new BusinessException("Cannot deactivate department with assigned users");
        }

        department.setStatus(status);
        department.setUpdatedAt(LocalDateTime.now());
        return toProjection(departmentRepository.save(department));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    @Transactional
    public DepartmentProjectionDTO renameDepartment(Long departmentId, String newName) {
        if (newName == null || newName.isBlank()) {
            throw new BusinessException("Department name is required");
        }

        String normalizedName = newName.strip().toUpperCase();
        DepartmentEntity department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BusinessException("Department not found"));

        departmentRepository.findByName(normalizedName)
                .filter(existing -> !existing.getId().equals(departmentId))
                .ifPresent(existing -> {
                    throw new BusinessException("Department name already in use");
                });

        department.setName(normalizedName);
        department.setUpdatedAt(LocalDateTime.now());
        return toProjection(departmentRepository.save(department));
    }

    private DepartmentProjectionDTO toProjection(DepartmentEntity department) {
        return new DepartmentProjectionDTO(
                department.getId(),
                department.getName(),
                department.getStatus(),
                userRepository.countByDepartmentId(department.getId())
        );
    }
}