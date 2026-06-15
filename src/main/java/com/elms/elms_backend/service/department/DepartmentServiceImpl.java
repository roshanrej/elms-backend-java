package com.elms.elms_backend.service.department;

import com.elms.elms_backend.dto.department.DepartmentProjectionDTO;
import com.elms.elms_backend.repository.department.DepartmentRepository;
import com.elms.elms_backend.repository.user.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

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

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public List<DepartmentProjectionDTO> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(department -> new DepartmentProjectionDTO(
                        department.getId(),
                        department.getName(),
                        department.getStatus(),
                        userRepository.countByDepartmentId(department.getId())
                ))
                .toList();
    }
}