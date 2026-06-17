package com.elms.elms_backend.service.user;

import com.elms.elms_backend.dto.user.CreateUserRequestDTO;
import com.elms.elms_backend.dto.user.UserProjectionDTO;
import com.elms.elms_backend.entity.DepartmentEntity;
import com.elms.elms_backend.entity.RoleEntity;
import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.entity.enums.DepartmentStatusEnum;
import com.elms.elms_backend.entity.enums.RoleEnum;
import com.elms.elms_backend.entity.enums.UserStatusEnum;
import com.elms.elms_backend.exception.BusinessException;
import com.elms.elms_backend.mapper.user.UserMapper;
import com.elms.elms_backend.repository.auth.RefreshTokenRepository;
import com.elms.elms_backend.repository.department.DepartmentRepository;
import com.elms.elms_backend.repository.user.RoleRepository;
import com.elms.elms_backend.repository.user.UserRepository;
import com.elms.elms_backend.service.leavebalance.LeaveBalanceService;
import com.elms.elms_backend.util.AssignableRoles;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SuperAdminServiceImpl implements SuperAdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final LeaveBalanceService leaveBalanceService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;

    public SuperAdminServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            DepartmentRepository departmentRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper,
            LeaveBalanceService leaveBalanceService,
            RefreshTokenRepository refreshTokenRepository,
            UserService userService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.leaveBalanceService = leaveBalanceService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userService = userService;
    }

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Transactional(readOnly = true)
    public List<UserProjectionDTO> listUsers() {
        return userRepository.findAllWithAssociations().stream()
                .map(userMapper::mapToUserProjectionDTO)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Transactional
    public UserProjectionDTO createUser(CreateUserRequestDTO request) {
        if (request.getName() == null
                || request.getEmail() == null
                || request.getPassword() == null
                || request.getDepartmentId() == null
                || request.getRoleId() == null) {
            throw new BusinessException("Missing required fields");
        }

        String email = request.getEmail().strip().toLowerCase();
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException("Email already exists");
        }

        DepartmentEntity department = departmentRepository
                .findByIdAndStatus(request.getDepartmentId(), DepartmentStatusEnum.ACTIVE)
                .orElseThrow(() -> new BusinessException("Invalid department"));

        RoleEntity role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new BusinessException("Invalid role"));

        if (!AssignableRoles.isAssignable(role.getName())) {
            throw new BusinessException("Cannot assign this role via user creation.");
        }

        UserEntity user = UserEntity.builder()
                .name(request.getName().strip())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .department(department)
                .status(UserStatusEnum.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        UserEntity savedUser = userRepository.save(user);
        if (role.getName() == RoleEnum.EMPLOYEE) {
            leaveBalanceService.provisionEmployeeBalances(savedUser);
        }

        return userMapper.mapToUserProjectionDTO(
                userRepository.findByIdWithAssociations(savedUser.getId()).orElse(savedUser)
        );
    }

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Transactional
    public UserProjectionDTO activateUser(Long userId) {
        UserEntity user = findUserOrThrow(userId);
        if (user.getStatus() == UserStatusEnum.ACTIVE) {
            throw new BusinessException("User is already active.");
        }

        user.setStatus(UserStatusEnum.ACTIVE);
        user.setUpdatedAt(LocalDateTime.now());
        return userMapper.mapToUserProjectionDTO(userRepository.save(user));
    }

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Transactional
    public UserProjectionDTO deactivateUser(Long userId) {
        UserEntity actor = userService.getAuthenticatedUser();
        if (actor.getId().equals(userId)) {
            throw new BusinessException("You cannot deactivate your own account.");
        }

        UserEntity user = findUserOrThrow(userId);
        if (user.getStatus() == UserStatusEnum.INACTIVE) {
            throw new BusinessException("User is already inactive.");
        }

        if (user.getRole().getName() == RoleEnum.SUPER_ADMIN) {
            long activeSuperAdmins = userRepository.countByRole_NameAndStatus(
                    RoleEnum.SUPER_ADMIN,
                    UserStatusEnum.ACTIVE
            );
            if (activeSuperAdmins <= 1) {
                throw new BusinessException("Cannot deactivate the last active super admin.");
            }
        }

        user.setStatus(UserStatusEnum.INACTIVE);
        user.setUpdatedAt(LocalDateTime.now());
        refreshTokenRepository.deleteByUser(user);
        return userMapper.mapToUserProjectionDTO(userRepository.save(user));
    }

    private UserEntity findUserOrThrow(Long userId) {
        return userRepository.findByIdWithAssociations(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
    }
}