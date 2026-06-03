package com.elms.elms_backend.service.leaverequest;

import com.elms.elms_backend.dto.leave.CreateLeaveRequestDTO;
import com.elms.elms_backend.dto.leave.EmployeeLeaveRequestDTO;
import com.elms.elms_backend.dto.leave.LeaveRequestProjectionDTO;
import com.elms.elms_backend.dto.leave.ManagerEmployeeLeaveDTO;
import com.elms.elms_backend.entity.LeaveRequestEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface LeaveRequestService {

    /**
     * Fetches authenticated employee leave requests.
     *
     * @return employee leave requests
     */
    public List<EmployeeLeaveRequestDTO>  getEmployeeActiveLeaveRequests();

    /**
     * Edits existing leave draft
     * @param id persisted leave draft id
     * @return LeaveRequestProjectionDTO  persisted updated leave draft
     */
    public LeaveRequestProjectionDTO editLeaveDraft(Long id, CreateLeaveRequestDTO createLeaveRequestDTO);


    /**
     * Cancels pending leave request  for authenticated employee.
     *
     * @param id incoming leave id
     * @return persisted leave request projection
     */
    LeaveRequestProjectionDTO cancelLeaveRequest(Long leaveRequestId);
    /**
     * Gets leave requests for logged in Employee
     * @return persisted leave Requests for authenticated employee
     */
    List<ManagerEmployeeLeaveDTO> getManagerOwnedLeaveRequests();




    /**
     * Creates and submits new leave request.
     *
     * @param requestDTO incoming leave submission payload
     * @return persisted submitted leave projection
     */
    LeaveRequestProjectionDTO submitNewLeaveRequest(
            CreateLeaveRequestDTO requestDTO
    );


    /**
     * Creates leave draft for authenticated employee.
     *
     * @param requestDTO incoming leave draft payload
     * @return persisted leave draft projection
     */
    LeaveRequestProjectionDTO createLeaveDraft(
            CreateLeaveRequestDTO requestDTO
    );


    /**
     * Submits existing leave draft.
     *
     * @param id leave request identifier

     * @return submitted leave projection
     */
    LeaveRequestProjectionDTO submitLeaveRequest(
            Long id
    );


    /**
     * Requests cancellation for approved leave.
     *
     * @param id leave request identifier
     * @return updated leave projection
     */
    LeaveRequestProjectionDTO requestLeaveCancel(
            Long id
    );


    /**
     * Approves pending leave request.
     *
     * @param id leave request identifier
     * @return approved leave projection
     */
    LeaveRequestProjectionDTO approveLeaveRequest(
            Long id
    );


    /**
     * Rejects pending leave request.
     *
     * @param id leave request identifier
     * @return rejected leave projection
     */
    LeaveRequestProjectionDTO rejectLeaveRequest(
            Long id
    );


    /**
     * Approves leave cancellation request.
     *
     * @param id leave request identifier
     * @return cancelled leave projection
     */
    LeaveRequestProjectionDTO approveCancelRequest(
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


    LeaveRequestProjectionDTO rejectCancelRequest(
            Long id
    );

    /**
     * Fetches authenticated employee leave request projections.
     *
     * @return employee leave request projections
     */
    List<LeaveRequestProjectionDTO>
    getEmployeeLeaveRequests();


    /**
     * Fetches authenticated employee leave drafts.
     *
     * @return employee draft projections
     */
    List<LeaveRequestProjectionDTO>
    getEmployeeLeaveDrafts();

}