package com.elms.elms_backend.service.user;

import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.repository.user.UserRepository;
import com.elms.elms_backend.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl implements  UserService{

    final UserRepository userRepo;
    public UserServiceImpl( UserRepository userRepo){
        this.userRepo = userRepo;
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




}
