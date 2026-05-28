package com.elms.elms_backend.config;

import com.elms.elms_backend.entity.LeaveTypeEntity;
import com.elms.elms_backend.entity.RoleEntity;
import com.elms.elms_backend.entity.DepartmentEntity;
import com.elms.elms_backend.entity.enums.DepartmentStatusEnum;
import com.elms.elms_backend.entity.enums.LeaveRequestStatusEnum;
import com.elms.elms_backend.entity.enums.LeaveTypeStatusEnum;
import com.elms.elms_backend.entity.enums.RoleEnum;
import com.elms.elms_backend.repository.department.DepartmentRepository;
import com.elms.elms_backend.repository.leave.LeaveTypeRepository;
import com.elms.elms_backend.repository.user.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class SeedDataConfig {

    private final RoleRepository roleRepo;
    private final DepartmentRepository departmentRepo;
    private final LeaveTypeRepository leaveTypeRepo;
    private final Set<String> departmentNames = new HashSet<>(
            Set.of(
                    "HR",
                    "ENGINEERING",
                    "FINANCE",
                    "MARKETING",
                    "SALES",
                    "OPERATIONS",
                    "ADMIN",
                    "SUPPORT"
            )
    );
    private final Set<String> leaveTypeNames = new HashSet<>(
            Set.of(
                    "CASUAL",
                    "ANNUAL",
                    "SICK",
                    "MATERNITY",
                    "PATERNITY"
            )
    );


    @Bean
    CommandLineRunner seedData() {

        return args -> {

            // ROLES

            if(roleRepo.count() == 0){

                roleRepo.save(
                        new RoleEntity(null, RoleEnum.ADMIN)
                );

                roleRepo.save(
                        new RoleEntity(null, RoleEnum.MANAGER)
                );

                roleRepo.save(
                        new RoleEntity(null, RoleEnum.EMPLOYEE)
                );
            }

            // DEPARTMENTS

            if(departmentRepo.count() == 0){
                for(String deptName : departmentNames){
                    departmentRepo.save(
                            DepartmentEntity.builder()
                                    .name(deptName)
                                    .createdAt(LocalDateTime.now())
                                    .status(DepartmentStatusEnum.ACTIVE)
                                    .build()
                    );
                }


            }

            // LEAVE TYPES

            if(leaveTypeRepo.count() == 0){
                for(String leaveTypeName: leaveTypeNames){
                    leaveTypeRepo.save(
                            LeaveTypeEntity.builder()
                                    .name(leaveTypeName)
                                    .status(LeaveTypeStatusEnum.ACTIVE)
                                    .build()
                    );

                }


            }
        };
    }
}