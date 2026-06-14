package com.elms.elms_backend.dto.leavepolicy;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateLeavePolicyResponseDTO {
  private Long id;
  private String leaveType;
  private Integer year;
  private Integer allocatedLeave;
  private Integer noticePeriodDays;

}
