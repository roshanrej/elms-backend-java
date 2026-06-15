package com.elms.elms_backend.service.team;


import com.elms.elms_backend.dto.team.CreateTeamDTO;
import com.elms.elms_backend.dto.team.CreateTeamResponseDTO;
import com.elms.elms_backend.entity.LeaveTypeEntity;
import com.elms.elms_backend.entity.TeamEntity;
import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.entity.enums.LeaveTypeStatusEnum;
import com.elms.elms_backend.repository.team.TeamRepository;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface TeamService {


    TeamEntity resolveTeam(
            String teamName
    );

    @PreAuthorize("hasRole('ADMIN)")
    CreateTeamResponseDTO createTeam(CreateTeamDTO createTeamDTO);

    @PreAuthorize("hasRole('ADMIN)")
    CreateTeamResponseDTO assignTeamManager(Long teamId, Long managerId);

    @PreAuthorize("hasRole('ADMIN')")
CreateTeamResponseDTO editTeam(Long teamId, CreateTeamDTO createTeamDTO);
}
