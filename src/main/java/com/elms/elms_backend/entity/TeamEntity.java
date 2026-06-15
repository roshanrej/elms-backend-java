package com.elms.elms_backend.entity;


import jakarta.persistence.*;
import lombok.*;
import com.elms.elms_backend.entity.UserEntity;

import java.util.List;

@Entity
@Table(name="teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamEntity {

    @Id
    private Long id;

    private String name;

    @OneToOne
    @JoinColumn(name = "manager_id", unique = true)
    private UserEntity manager;

    @OneToMany(mappedBy = "team")
    private List<UserEntity> members;
}
