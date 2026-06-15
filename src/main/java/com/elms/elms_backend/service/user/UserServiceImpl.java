package com.elms.elms_backend.service.user;

import com.elms.elms_backend.entity.LeaveRequestEntity;
import com.elms.elms_backend.entity.RoleEntity;
import com.elms.elms_backend.entity.TeamEntity;
import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.entity.enums.RoleEnum;
import com.elms.elms_backend.entity.enums.UserStatusEnum;
import com.elms.elms_backend.repository.user.RoleRepository;
import com.elms.elms_backend.repository.user.UserRepository;
import com.elms.elms_backend.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UserServiceImpl implements  UserService{

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;

    public UserServiceImpl(UserRepository userRepo, RoleRepository roleRepo){
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
    }

    /**
     * Retrieves the currently authenticated user
     * from the Spring Security context.
     *
     * @return authenticated user entity
     */
    @Override
    public UserEntity getAuthenticatedUser() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();

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

}
