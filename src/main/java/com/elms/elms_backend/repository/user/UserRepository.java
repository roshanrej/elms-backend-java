package com.elms.elms_backend.repository.user;

import com.elms.elms_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {

}



