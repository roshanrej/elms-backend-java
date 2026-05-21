package com.elms.elms_backend.service.leave;

import com.elms.elms_backend.dto.leave.CreateLeaveRequestDTO;
import com.elms.elms_backend.dto.leave.CreateLeaveRequestResponseDTO;
import com.elms.elms_backend.entity.LeaveRequestEntity;
import com.elms.elms_backend.entity.enums.LeaveRequestActionEnum;

import java.util.List;

public interface LeaveRequestService {
    /**
     * Gets total consumed leaves for leave type
     *
     * @param leaveTypeId id for leaveType
     * @return persisted number of leave days for type
     */



    /**
     * Creates and submits new leave request.
     *
     * @param requestDTO incoming leave submission payload
     * @return persisted submitted leave projection
     */
    CreateLeaveRequestResponseDTO submitNewLeaveRequest(
            CreateLeaveRequestDTO requestDTO
    );


    /**
     * Creates leave draft for authenticated employee.
     *
     * @param requestDTO incoming leave draft payload
     * @return persisted leave draft projection
     */
    CreateLeaveRequestResponseDTO createLeaveDraft(
            CreateLeaveRequestDTO requestDTO
    );


    /**
     * Submits existing leave draft.
     *
     * @param id leave request identifier
     * @param createLeaveRequestDTO updated leave submission payload
     * @return submitted leave projection
     */
    CreateLeaveRequestResponseDTO submitLeaveRequest(
            Long id,
            CreateLeaveRequestDTO createLeaveRequestDTO
    );


    /**
     * Requests cancellation for approved leave.
     *
     * @param id leave request identifier
     * @return updated leave projection
     */
    CreateLeaveRequestResponseDTO requestLeaveCancel(
            Long id
    );


    /**
     * Approves pending leave request.
     *
     * @param id leave request identifier
     * @return approved leave projection
     */
    CreateLeaveRequestResponseDTO approveLeaveRequest(
            Long id
    );


    /**
     * Rejects pending leave request.
     *
     * @param id leave request identifier
     * @return rejected leave projection
     */
    CreateLeaveRequestResponseDTO rejectLeaveRequest(
            Long id
    );


    /**
     * Approves leave cancellation request.
     *
     * @param id leave request identifier
     * @return cancelled leave projection
     */
    CreateLeaveRequestResponseDTO approveCancelRequest(
            Long id
    );


    /**
     * Rejects leave cancellation request.
     *
     * @param id leave request identifier
     * @return restored approved leave projection
     */
    CreateLeaveRequestResponseDTO rejectCancelRequest(
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
    List<CreateLeaveRequestResponseDTO>
    getEmployeeLeaveRequests();


    /**
     * Fetches authenticated employee leave drafts.
     *
     * @return employee draft projections
     */
    List<CreateLeaveRequestResponseDTO>
    getEmployeeLeaveDrafts();


    /**
     * Resolves allowed workflow actions
     * for authenticated user.
     *
     * @param leaveRequest target leave request
     * @return allowed workflow actions
     */
    List<LeaveRequestActionEnum>
    allowedLeaveActions(
            LeaveRequestEntity leaveRequest
    );
}