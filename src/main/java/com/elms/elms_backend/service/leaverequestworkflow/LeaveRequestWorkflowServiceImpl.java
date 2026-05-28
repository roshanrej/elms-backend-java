package com.elms.elms_backend.service.leaverequestworkflow;

import com.elms.elms_backend.entity.LeaveRequestEntity;
import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.entity.enums.LeaveRequestActionEnum;
import com.elms.elms_backend.entity.enums.LeaveRequestStatusEnum;
import com.elms.elms_backend.entity.enums.RoleEnum;
import com.elms.elms_backend.service.user.UserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LeaveRequestWorkflowServiceImpl implements LeaveRequestWorkflowService {
    // =========================================================================
    // WORKFLOW AUTHORIZATION ENGINE
    // =========================================================================
    private final UserService userService;

    public LeaveRequestWorkflowServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public List<LeaveRequestActionEnum> allowedLeaveActions(
            LeaveRequestEntity leaveRequest
    ) {

        UserEntity user =
                userService.getAuthenticatedUser();

        RoleEnum userRole =
                user.getRole().getName();

        LeaveRequestStatusEnum status =
                leaveRequest.getStatus();

        boolean isOwner =
                user.getId().equals(
                        leaveRequest.getEmployee().getId()
                );

        List<LeaveRequestActionEnum> actions =
                new ArrayList<>();

        if (userRole == RoleEnum.EMPLOYEE) {

            if (isOwner && status == LeaveRequestStatusEnum.DRAFT) {

                actions.add(LeaveRequestActionEnum.DELETE_DRAFT);
                actions.add(LeaveRequestActionEnum.EDIT_DRAFT);
                actions.add(LeaveRequestActionEnum.SUBMIT_REQUEST);
            }

            if (isOwner && status == LeaveRequestStatusEnum.PENDING) {

                actions.add(LeaveRequestActionEnum.CANCEL_REQUEST);
            }

            if (isOwner && status == LeaveRequestStatusEnum.APPROVED) {

                actions.add(LeaveRequestActionEnum.REQUEST_CANCEL);
            }
        }

        if (userRole == RoleEnum.MANAGER) {

            if (status == LeaveRequestStatusEnum.PENDING) {

                actions.add(LeaveRequestActionEnum.APPROVE_REQUEST);
                actions.add(LeaveRequestActionEnum.REJECT_REQUEST);
            }

            if (status == LeaveRequestStatusEnum.CANCEL_PENDING) {

                actions.add(LeaveRequestActionEnum.APPROVE_CANCEL);
                actions.add(LeaveRequestActionEnum.REJECT_CANCEL);
            }
        }

        return actions;
    }


    // =========================================================================
    // WORKFLOW VALIDATION
    // =========================================================================

    @Override
    public void validateTransition(
            LeaveRequestEntity leaveRequest,
            LeaveRequestActionEnum action
    ) {

        List<LeaveRequestActionEnum> actions =
                allowedLeaveActions(leaveRequest);

        if (!actions.contains(action)) {
            throw new IllegalStateException(
                    "You are not allowed to perform this action."
            );
        }
    }
}
