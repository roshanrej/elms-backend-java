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

@Configuration
@RequiredArgsConstructor
public class SeedDataConfig {

    private final RoleRepository roleRepo;
    private final DepartmentRepository departmentRepo;
    private final LeaveTypeRepository leaveTypeRepo;

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

                departmentRepo.save(
                        DepartmentEntity.builder()
                                .name("HUMAN_RESOURCES")
                                .createdAt(LocalDateTime.now())
                                .status(DepartmentStatusEnum.ACTIVE)
                                .build()
                );

                departmentRepo.save(
                        DepartmentEntity.builder()
                                .name("ENGINEERING")
                                .createdAt(LocalDateTime.now())
                                .status(DepartmentStatusEnum.ACTIVE)
                                .build()
                );
            }

            // LEAVE TYPES

            if(leaveTypeRepo.count() == 0){

                leaveTypeRepo.save(
                        LeaveTypeEntity.builder()
                                .name("SICK")
                                .status(LeaveTypeStatusEnum.ACTIVE)
                                .build()
                );

                leaveTypeRepo.save(
                        LeaveTypeEntity.builder()
                                .name("CASUAL")
                                .status(LeaveTypeStatusEnum.ACTIVE)
                                .build()
                );
            }
        };
    }
}