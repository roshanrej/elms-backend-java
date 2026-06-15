package com.elms.elms_backend.mapper.user;

import com.elms.elms_backend.dto.user.UserProjectionDTO;
import com.elms.elms_backend.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserProjectionDTO mapToUserProjectionDTO(UserEntity user){
        String teamName = "";
        if(user.getTeam() != null){
            teamName = user.getTeam().getName();
        }
        return new UserProjectionDTO(
                user.getId(),
                teamName,
                user.getEmail(),
                user.getName(),
                user.getRole().getName(),
                user.getDepartment().getName(),
                user.getStatus()
        );
    }
}
