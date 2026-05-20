package com.elms.elms_backend.service.user;

import com.elms.elms_backend.entity.LeaveRequestEntity;
import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.entity.enums.LeaveActionEnum;

import java.util.List;

public interface UserService {
    UserEntity getAuthenticatedUser();


}
