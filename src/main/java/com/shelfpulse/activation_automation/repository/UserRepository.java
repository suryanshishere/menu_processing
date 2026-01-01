package com.shelfpulse.activation_automation.repository;

import com.shelfpulse.activation_automation.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
