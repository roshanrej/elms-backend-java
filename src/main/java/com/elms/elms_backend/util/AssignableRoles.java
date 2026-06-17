package com.elms.elms_backend.util;

import com.elms.elms_backend.entity.enums.RoleEnum;

import java.util.EnumSet;
import java.util.Set;

public final class AssignableRoles {

    public static final Set<RoleEnum> ASSIGNABLE = EnumSet.of(
            RoleEnum.ADMIN,
            RoleEnum.MANAGER,
            RoleEnum.EMPLOYEE
    );

    private AssignableRoles() {
    }

    public static boolean isAssignable(RoleEnum role) {
        return ASSIGNABLE.contains(role);
    }
}