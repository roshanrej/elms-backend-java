package com.elms.elms_backend.controller.team;


import com.elms.elms_backend.dto.api.ApiResponseDTO;
import com.elms.elms_backend.dto.team.CreateTeamDTO;
import com.elms.elms_backend.dto.team.CreateTeamResponseDTO;
import com.elms.elms_backend.entity.TeamEntity;
import com.elms.elms_backend.service.team.TeamService;
import com.elms.elms_backend.util.ResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("admin/api/teams")
public class AdminTeamController {
    private final TeamService teamService;

    public AdminTeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponseDTO<?>> createTeam(@RequestBody CreateTeamDTO createTeamDTO){
    CreateTeamResponseDTO response = teamService.createTeam(createTeamDTO);
    return ResponseHandler.success(response,
            "Team successfully created.",
            HttpStatus.CREATED);
    }

    @PatchMapping("/{teamId}/manager/{managerId}")
    public ResponseEntity<ApiResponseDTO<?>> assignTeamManager(@PathVariable Long teamId, @PathVariable Long managerId){
        CreateTeamResponseDTO response = teamService.assignTeamManager(teamId, managerId);
        return ResponseHandler.success(response,
                "Manager assigned successfully.",
                HttpStatus.OK);
    }

    @PostMapping("/{teamId}/edit")
    public ResponseEntity<ApiResponseDTO<?>> editTeam(@PathVariable Long teamId, @RequestBody CreateTeamDTO dto){
        CreateTeamResponseDTO response = teamService.editTeam(teamId,dto);
        return ResponseHandler.success(response,
                "Team edited successfully.",
                HttpStatus.OK);
    }


}
