package com.elms.elms_backend.service.leave;


import com.elms.elms_backend.entity.LeaveRequest;
import com.elms.elms_backend.entity.enums.LeaveRequestStatusEnum;
import com.elms.elms_backend.repository.leave.LeaveRequestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository repo;

    public LeaveRequestServiceImpl(LeaveRequestRepository repo) {
        this.repo = repo;
    }

    @Override
    public LeaveRequest createLeave(LeaveRequest request) {

        request.setStatus(LeaveRequestStatusEnum.DRAFT);
        request.setCreatedAt(LocalDateTime.now());
        request.setYear(request.getStartDate().getYear());

        return repo.save(request);
    }
}