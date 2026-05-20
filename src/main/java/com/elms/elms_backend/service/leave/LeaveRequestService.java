package com.elms.elms_backend.service.leave;

import com.elms.elms_backend.dto.leave.LeaveRequestDTO;
import com.elms.elms_backend.dto.leave.LeaveResponseDTO;
import com.elms.elms_backend.entity.LeaveRequestEntity;
import com.elms.elms_backend.entity.enums.LeaveActionEnum;

import java.util.List;

public interface LeaveRequestService {
    /**
     * Gets total consumed leaves for leave type
     *
     * @param leaveTypeId id for leaveType
     * @return persisted number of leave days for type
     */
      Integer getTotalConsumedLeaves(Long leaveTypeId, Integer Year);


    /**
     * Creates and submits new leave request.
     *
     * @param requestDTO incoming leave submission payload
     * @return persisted submitted leave projection
     */
    LeaveResponseDTO submitNewLeaveRequest(
            LeaveRequestDTO requestDTO
    );


    /**
     * Creates leave draft for authenticated employee.
     *
     * @param requestDTO incoming leave draft payload
     * @return persisted leave draft projection
     */
    LeaveResponseDTO createLeaveDraft(
            LeaveRequestDTO requestDTO
    );


    /**
     * Submits existing leave draft.
     *
     * @param id leave request identifier
     * @param leaveRequestDTO updated leave submission payload
     * @return submitted leave projection
     */
    LeaveResponseDTO submitLeaveRequest(
            Long id,
            LeaveRequestDTO leaveRequestDTO
    );


    /**
     * Requests cancellation for approved leave.
     *
     * @param id leave request identifier
     * @return updated leave projection
     */
    LeaveResponseDTO requestLeaveCancel(
            Long id
    );


    /**
     * Approves pending leave request.
     *
     * @param id leave request identifier
     * @return approved leave projection
     */
    LeaveResponseDTO approveLeaveRequest(
            Long id
    );


    /**
     * Rejects pending leave request.
     *
     * @param id leave request identifier
     * @return rejected leave projection
     */
    LeaveResponseDTO rejectLeaveRequest(
            Long id
    );


    /**
     * Approves leave cancellation request.
     *
     * @param id leave request identifier
     * @return cancelled leave projection
     */
    LeaveResponseDTO approveCancelRequest(
            Long id
    );


    /**
     * Rejects leave cancellation request.
     *
     * @param id leave request identifier
     * @return restored approved leave projection
     */
    LeaveResponseDTO rejectCancelRequest(
            Long id
    );


    /**
     * Deletes employee leave draft.
     *
     * @param id leave request identifier
     */
    void deleteLeaveDraft(
            Long id
    );


    /**
     * Fetches authenticated employee leave requests.
     *
     * @return employee leave request projections
     */
    List<LeaveResponseDTO>
    getEmployeeLeaveRequests();


    /**
     * Fetches authenticated employee leave drafts.
     *
     * @return employee draft projections
     */
    List<LeaveResponseDTO>
    getEmployeeLeaveDrafts();


    /**
     * Resolves allowed workflow actions
     * for authenticated user.
     *
     * @param leaveRequest target leave request
     * @return allowed workflow actions
     */
    List<LeaveActionEnum>
    allowedLeaveActions(
            LeaveRequestEntity leaveRequest
    );
}