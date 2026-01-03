package com.shelfpulse.activation_automation.repository;

import com.shelfpulse.activation_automation.entity.Menu;
import com.shelfpulse.activation_automation.enums.MenuStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Integer> {

    @Query("SELECT COUNT(m) FROM Menu m WHERE m.status = :status AND m.eatery.admin.id = :adminId")
    long countByStatusAndAdminId(@Param("status") MenuStatus status, @Param("adminId") Integer adminId);

    @Query("SELECT COUNT(m) FROM Menu m WHERE m.status = :status")
    long countByStatus(@Param("status") MenuStatus status);
}
