package com.elms.elms_backend.security;

import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.entity.enums.UserStatusEnum;
import com.elms.elms_backend.repository.user.UserRepository;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepo;

    public CustomUserDetailsService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        UserEntity user =
                userRepo.findByEmail(email)
                        .orElseThrow(
                                () -> new UsernameNotFoundException(
                                        "User not found."
                                )
                        );

        if(user.getStatus() != UserStatusEnum.ACTIVE){

            throw new DisabledException(
                    "User account inactive."
            );
        }

        return new UserPrincipal(user);
    }
}
