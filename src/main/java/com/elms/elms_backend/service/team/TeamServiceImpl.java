package com.elms.elms_backend.service.team;

import com.elms.elms_backend.dto.team.CreateTeamDTO;
import com.elms.elms_backend.dto.team.CreateTeamResponseDTO;
import com.elms.elms_backend.entity.TeamEntity;
import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.entity.enums.RoleEnum;
import com.elms.elms_backend.entity.enums.UserStatusEnum;
import com.elms.elms_backend.repository.team.TeamRepository;
import com.elms.elms_backend.repository.user.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;


@Service
public class TeamServiceImpl implements TeamService {


    private final TeamRepository teamRepo;
    private final UserRepository userRepo;


    public TeamServiceImpl(TeamRepository teamRepo, UserRepository userRepo) {
        this.teamRepo = teamRepo;
        this.userRepo = userRepo;
        ;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public TeamEntity resolveTeam(
            String teamName
    ) {
        return teamRepo.findByName(teamName).orElseThrow(
                () -> new RuntimeException("Invalid team")
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public CreateTeamResponseDTO createTeam(CreateTeamDTO createTeamDTO) {
        if (createTeamDTO.getName() == null) {
            throw new IllegalArgumentException("Team name cant be blank");
        }
        TeamEntity team = TeamEntity.builder().name(createTeamDTO.getName()).build();
        TeamEntity savedTeam = teamRepo.save(team);
        return new CreateTeamResponseDTO(savedTeam.getId(), savedTeam.getName(), savedTeam.getManager().getName());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public CreateTeamResponseDTO assignTeamManager(Long teamId, Long managerId) {
        TeamEntity team = teamRepo.findById(teamId).orElseThrow(() -> new
                RuntimeException("Invalid team"));
        UserEntity user = userRepo.findByIdAndStatus(managerId, UserStatusEnum.ACTIVE).orElseThrow(() ->
                new RuntimeException("Inactive user"));
        if (user.getRole().getName() != RoleEnum.MANAGER) {
            throw new IllegalArgumentException("Team assignment requires a manager role");
        }
        team.setManager(user);
        TeamEntity savedTeam = teamRepo.save(team);
        return new CreateTeamResponseDTO(savedTeam.getId(), savedTeam.getName(), savedTeam.getManager().getName());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public CreateTeamResponseDTO editTeam(Long teamId, CreateTeamDTO createTeamDTO) {
        TeamEntity team = teamRepo.findById(teamId).orElseThrow(() -> new
                RuntimeException("Invalid team"));
        if (createTeamDTO.getName() == null) {
            throw new IllegalArgumentException("Team name cant be blank");
        }
        team.setName(createTeamDTO.getName());
        TeamEntity savedTeam = teamRepo.save(team);
        return new CreateTeamResponseDTO(savedTeam.getId(), savedTeam.getName(), savedTeam.getManager().getName());
    }
}