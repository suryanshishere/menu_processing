package com.shelfpulse.activation_automation.repository;

import com.shelfpulse.activation_automation.entity.Menu;
import com.shelfpulse.activation_automation.enums.MenuStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Integer>, JpaSpecificationExecutor<Menu> {

    @Query("SELECT COUNT(m) FROM Menu m WHERE m.status = :status AND m.eatery.admin.id = :adminId")
    long countByStatusAndAdminId(@Param("status") MenuStatus status, @Param("adminId") Integer adminId);

    @Query("SELECT COUNT(m) FROM Menu m WHERE m.status = :status")
    long countByStatus(@Param("status") MenuStatus status);

    Optional<Menu> findFirstByEateryIdOrderByCreatedAtDesc(Integer eateryId);

    @Query("SELECT m FROM Menu m WHERE m.eatery.id = :eateryId AND m.admin.id = :adminId ORDER BY m.createdAt DESC LIMIT 1")
    Optional<Menu> findFirstByEateryIdAndAdminIdOrderByCreatedAtDesc(@Param("eateryId") Integer eateryId,
            @Param("adminId") Integer adminId);

    long countByEateryId(Integer eateryId);
}
