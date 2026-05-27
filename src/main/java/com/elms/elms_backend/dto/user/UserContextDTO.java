package com.elms.elms_backend.dto.user;

import com.elms.elms_backend.entity.enums.RoleEnum;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter

public class UserContextDTO {
    private String name;
    private String email;
    private RoleEnum role;
    private String department;
    public UserContextDTO(String name, String email, RoleEnum role, String department){
        this.name = name;
        this.email = email;
        this.role = role;
        this.department  = department;

    }
}
