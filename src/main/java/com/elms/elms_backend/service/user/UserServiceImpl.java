package com.elms.elms_backend.service.user;

import com.elms.elms_backend.dto.user.UserProjectionDTO;
import com.elms.elms_backend.entity.DepartmentEntity;
import com.elms.elms_backend.entity.RoleEntity;
import com.elms.elms_backend.entity.TeamEntity;
import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.entity.enums.DepartmentStatusEnum;
import com.elms.elms_backend.entity.enums.RoleEnum;
import com.elms.elms_backend.entity.enums.UserStatusEnum;
import com.elms.elms_backend.exception.BusinessException;
import com.elms.elms_backend.mapper.user.UserMapper;
import com.elms.elms_backend.repository.department.DepartmentRepository;
import com.elms.elms_backend.repository.team.TeamRepository;
import com.elms.elms_backend.repository.user.RoleRepository;
import com.elms.elms_backend.repository.user.UserRepository;
import com.elms.elms_backend.security.UserPrincipal;
import com.elms.elms_backend.service.leavebalance.LeaveBalanceService;
import com.elms.elms_backend.util.AssignableRoles;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class UserServiceImpl implements  UserService{

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final TeamRepository teamRepo;
    private final DepartmentRepository departmentRepository;
    private final UserMapper userMapper;
    private final LeaveBalanceService leaveBalanceService;

    public UserServiceImpl(
            UserRepository userRepo,
            RoleRepository roleRepo,
            TeamRepository teamRepo,
            DepartmentRepository departmentRepository,
            UserMapper userMapper,
            LeaveBalanceService leaveBalanceService
    ) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.teamRepo = teamRepo;
        this.departmentRepository = departmentRepository;
        this.userMapper = userMapper;
        this.leaveBalanceService = leaveBalanceService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserEntity getAuthenticatedUser() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new RuntimeException("Unauthorized");
        }

        return userRepo.findByEmail(
                        principal.getUsername()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );
    }

    @Override
    public List<UserEntity> findByRole(RoleEnum roleName) {
        RoleEntity role = roleRepo.findByName(roleName).orElseThrow(()->new IllegalArgumentException("Invalid role"));
        return  userRepo.findByRole(role);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<UserProjectionDTO> getAllUsers() {
        return userRepo.findAllWithAssociations().stream()
                .map(userMapper::mapToUserProjectionDTO)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<UserProjectionDTO> getActiveUsers() {
        return userRepo.findByStatusWithAssociations(UserStatusEnum.ACTIVE).stream()
                .map(userMapper::mapToUserProjectionDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProjectionDTO> getUsersByRoleAndStatus(RoleEnum roleName, UserStatusEnum status) {
        RoleEntity role = roleRepo.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid role"));
        return userRepo.findByRoleAndStatusWithAssociations(role, status).stream()
                .map(userMapper::mapToUserProjectionDTO)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserProjectionDTO assignUserToTeam(Long userId, Long teamId) {
        UserEntity user = userRepo.findByIdAndStatusWithAssociations(userId, UserStatusEnum.ACTIVE)
                .orElseThrow(() -> new BusinessException("User not found"));

        assertAdminCanModify(user);

        if (user.getRole().getName() != RoleEnum.EMPLOYEE) {
            throw new BusinessException("Only employees can be assigned to a team");
        }

        TeamEntity team = teamRepo.findById(teamId)
                .orElseThrow(() -> new BusinessException("Team not found"));

        user.setTeam(team);
        user.setUpdatedAt(LocalDateTime.now());
        return userMapper.mapToUserProjectionDTO(userRepo.save(user));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserProjectionDTO assignRole(Long userId, Long roleId) {
        UserEntity user = userRepo.findByIdWithAssociations(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        RoleEntity newRole = roleRepo.findById(roleId)
                .orElseThrow(() -> new BusinessException("Invalid role"));

        if (!AssignableRoles.isAssignable(newRole.getName())) {
            throw new BusinessException("This role cannot be assigned.");
        }

        assertAdminCanModify(user);

        RoleEnum oldRole = user.getRole().getName();
        if (oldRole == newRole.getName()) {
            return userMapper.mapToUserProjectionDTO(user);
        }

        if (oldRole == RoleEnum.MANAGER && newRole.getName() != RoleEnum.MANAGER) {
            teamRepo.clearManagerByManagerId(user.getId());
        }

        if (newRole.getName() != RoleEnum.EMPLOYEE) {
            user.setTeam(null);
        }

        user.setRole(newRole);
        user.setUpdatedAt(LocalDateTime.now());
        UserEntity savedUser = userRepo.save(user);
        leaveBalanceService.handleRoleChangeBalances(savedUser, oldRole, newRole.getName());
        return userMapper.mapToUserProjectionDTO(savedUser);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserProjectionDTO assignDepartment(Long userId, Long departmentId) {
        UserEntity user = userRepo.findByIdWithAssociations(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        assertAdminCanModify(user);

        DepartmentEntity department = departmentRepository
                .findByIdAndStatus(departmentId, DepartmentStatusEnum.ACTIVE)
                .orElseThrow(() -> new BusinessException("Invalid department"));

        user.setDepartment(department);
        user.setUpdatedAt(LocalDateTime.now());
        return userMapper.mapToUserProjectionDTO(userRepo.save(user));
    }

    private void assertAdminCanModify(UserEntity user) {
        if (user.getRole().getName() == RoleEnum.SUPER_ADMIN) {
            throw new BusinessException("Super admin accounts cannot be modified.");
        }
    }
}