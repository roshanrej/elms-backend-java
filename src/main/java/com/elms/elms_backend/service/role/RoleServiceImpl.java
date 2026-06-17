package com.elms.elms_backend.service.role;

import com.elms.elms_backend.dto.role.RoleProjectionDTO;
import com.elms.elms_backend.entity.RoleEntity;
import com.elms.elms_backend.repository.user.RoleRepository;
import com.elms.elms_backend.util.AssignableRoles;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleProjectionDTO> getAssignableRoles() {
        return roleRepository.findAll().stream()
                .filter(role -> AssignableRoles.isAssignable(role.getName()))
                .sorted(Comparator.comparing(RoleEntity::getId))
                .map(role -> new RoleProjectionDTO(role.getId(), role.getName()))
                .toList();
    }
}