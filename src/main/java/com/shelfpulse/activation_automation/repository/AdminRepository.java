package com.shelfpulse.activation_automation.repository;

import com.shelfpulse.activation_automation.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {
    Optional<Admin> findByEmail(String email);

    Optional<Admin> findByUsername(String username);

    Optional<Admin> findByEmailOrUsername(String email, String username);
}
