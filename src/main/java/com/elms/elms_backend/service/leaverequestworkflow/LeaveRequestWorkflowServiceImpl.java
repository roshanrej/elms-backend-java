package com.elms.elms_backend.service.leaverequestworkflow;

import com.elms.elms_backend.entity.LeaveRequestEntity;
import com.elms.elms_backend.entity.TeamEntity;
import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.entity.enums.LeaveRequestActionEnum;
import com.elms.elms_backend.entity.enums.LeaveRequestStatusEnum;
import com.elms.elms_backend.entity.enums.RoleEnum;
import com.elms.elms_backend.entity.enums.UserStatusEnum;
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

        if (userRole == RoleEnum.MANAGER && isManagerForLeaveRequest(user, leaveRequest)) {
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

    private boolean isManagerForLeaveRequest(UserEntity manager, LeaveRequestEntity leaveRequest) {
        TeamEntity team = leaveRequest.getEmployee().getTeam();
        return team != null
                && team.getManager() != null
                && manager.getId().equals(team.getManager().getId());
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

    /**
     * Asserts that the employee is in a position to have a leave request created.
     *
     * A leave request requires an active manager in the chain of approval.
     * Without one, the request would be permanently stuck in PENDING with no
     * one able to act on it — which is a worse outcome than blocking creation.
     *
     * @throws IllegalStateException if no active manager can be resolved
     */
    @Override
    public void assertEmployeeCanSubmitLeave(UserEntity employee) {
        if (employee.getTeam() == null) {
            throw new IllegalStateException(
                    "Leave request cannot be created: employee is not assigned to a team."
            );
        }
        UserEntity manager = employee.getTeam().getManager();
        if (manager == null) {
            throw new IllegalStateException(
                    "Leave request cannot be created: employee's team has no assigned manager."
            );
        }
        if (manager.getStatus() == UserStatusEnum.INACTIVE) {
            throw new IllegalStateException(
                    "Leave request cannot be created: employee's assigned manager is currently inactive."
            );
        }
    }

    /**
     * Asserts that the authenticated manager is authorized to act on the given
     * leave request, and that the employee is still active.
     * @throws IllegalStateException if the authenticated manager does not own this employee's team
     * or if the employee is inactive
     */
    @Override
    public void assertManagerCanPerformAction(LeaveRequestEntity leaveRequest) {
        UserEntity employee = leaveRequest.getEmployee();

        if (employee.getStatus() == UserStatusEnum.INACTIVE) {
            throw new IllegalStateException(
                    "Leave request cannot be acted on: employee is currently inactive."
            );
        }

        TeamEntity team = employee.getTeam();

        if (team == null || team.getManager() == null) {
            throw new IllegalStateException(
                    "Leave request cannot be acted on: employee has no assigned team or manager."
            );
        }

        UserEntity authenticatedManager = userService.getAuthenticatedUser();

        if (!authenticatedManager.getId().equals(team.getManager().getId())) {
            throw new IllegalStateException(
                    "Manager is not authorized to act on this leave request."
            );
        }
    }



}
