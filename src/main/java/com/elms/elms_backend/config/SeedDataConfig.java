package com.elms.elms_backend.config;

import com.elms.elms_backend.entity.DepartmentEntity;
import com.elms.elms_backend.entity.LeaveTypeEntity;
import com.elms.elms_backend.entity.RoleEntity;
import com.elms.elms_backend.entity.UserEntity;
import com.elms.elms_backend.entity.enums.DepartmentStatusEnum;
import com.elms.elms_backend.entity.enums.LeaveTypeStatusEnum;
import com.elms.elms_backend.entity.enums.RoleEnum;
import com.elms.elms_backend.entity.enums.UserStatusEnum;
import com.elms.elms_backend.repository.department.DepartmentRepository;
import com.elms.elms_backend.repository.leave.LeaveTypeRepository;
import com.elms.elms_backend.repository.user.RoleRepository;
import com.elms.elms_backend.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class SeedDataConfig {

    private final RoleRepository roleRepo;
    private final DepartmentRepository departmentRepo;
    private final LeaveTypeRepository leaveTypeRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${elms.super-admin.email:superadmin@elms.local}")
    private String superAdminEmail;

    @Value("${elms.super-admin.password:SuperAdmin123!}")
    private String superAdminPassword;

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
            for (RoleEnum roleName : RoleEnum.values()) {
                roleRepo.findByName(roleName).orElseGet(() -> roleRepo.save(new RoleEntity(null, roleName)));
            }

            if (departmentRepo.count() == 0) {
                for (String deptName : departmentNames) {
                    departmentRepo.save(
                            DepartmentEntity.builder()
                                    .name(deptName)
                                    .createdAt(LocalDateTime.now())
                                    .status(DepartmentStatusEnum.ACTIVE)
                                    .build()
                    );
                }
            }

            if (leaveTypeRepo.count() == 0) {
                for (String leaveTypeName : leaveTypeNames) {
                    leaveTypeRepo.save(
                            LeaveTypeEntity.builder()
                                    .name(leaveTypeName)
                                    .status(LeaveTypeStatusEnum.ACTIVE)
                                    .build()
                    );
                }
            }

            RoleEntity superRole = roleRepo.findByName(RoleEnum.SUPER_ADMIN)
                    .orElseThrow(() -> new IllegalStateException("SUPER_ADMIN role missing"));

            if (userRepo.findByRole(superRole).isEmpty()) {
                DepartmentEntity department = departmentRepo.findByName("ADMIN")
                        .or(() -> departmentRepo.findAll().stream().findFirst())
                        .orElseThrow(() -> new IllegalStateException("No department available for super admin"));

                UserEntity superAdmin = UserEntity.builder()
                        .name("Super Admin")
                        .email(superAdminEmail.strip().toLowerCase())
                        .passwordHash(passwordEncoder.encode(superAdminPassword))
                        .role(superRole)
                        .department(department)
                        .status(UserStatusEnum.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .build();
                userRepo.save(superAdmin);
            }
        };
    }
}