package com.elms.elms_backend.service.user;

import com.elms.elms_backend.dto.user.UserProjectionDTO;
import com.elms.elms_backend.entity.LeaveRequestEntity;
import com.elms.elms_backend.entity.RoleEntity;
import com.elms.elms_backend.entity.TeamEntity;
import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.entity.enums.RoleEnum;
import com.elms.elms_backend.entity.enums.UserStatusEnum;
import com.elms.elms_backend.mapper.user.UserMapper;
import com.elms.elms_backend.repository.user.RoleRepository;
import com.elms.elms_backend.repository.user.UserRepository;
import com.elms.elms_backend.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class UserServiceImpl implements  UserService{

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepo, RoleRepository roleRepo, UserMapper userMapper){
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.userMapper = userMapper;
    }

    /**
     * Retrieves the currently authenticated user
     * from the Spring Security context.
     *
     * @return authenticated user entity
     */
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

    /**
     * @param roleName
     * @return List of users based on role
     */
    @Override
    public List<UserEntity> findByRole(RoleEnum roleName) {
        RoleEntity role = roleRepo.findByName(roleName).orElseThrow(()->new IllegalArgumentException("Invalid role"));
        return  userRepo.findByRole(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProjectionDTO> getAllUsers() {
        return userRepo.findAllWithAssociations().stream()
                .map(userMapper::mapToUserProjectionDTO)
                .toList();
    }

    @Override
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

    @Transactional
    public UserProjectionDTO activateUser(Long Id) {
        UserEntity user = userRepo.findById(Id).orElseThrow(() ->
                new RuntimeException("User does not exist")
        );
        if (user.getStatus() != UserStatusEnum.INACTIVE) {
            throw new IllegalStateException("Invalid action");
        }
        user.setStatus(UserStatusEnum.ACTIVE);
       UserEntity savedUser =  userRepo.save(user);
       return userMapper.mapToUserProjectionDTO(savedUser);

    }

}
