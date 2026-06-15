package com.elms.elms_backend.dto.team;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class CreateTeamResponseDTO {
    private Long id;
    private String teamName;
    private String managerName;
}
