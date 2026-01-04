package com.shelfpulse.activation_automation.repository;

import com.shelfpulse.activation_automation.entity.Eatery;
import com.shelfpulse.activation_automation.enums.MenuStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EateryRepository extends JpaRepository<Eatery, Integer>, JpaSpecificationExecutor<Eatery> { // Added
                                                                                                             // JpaSpecificationExecutor

    @Query("SELECT COUNT(e) FROM Eatery e WHERE e.status <> :status AND e.admin.id = :adminId")
    long countByStatusNotAndAdminId(@Param("status") MenuStatus status, @Param("adminId") Integer adminId);

    @Query("SELECT COUNT(e) FROM Eatery e WHERE e.status <> :status")
    long countByStatusNot(@Param("status") MenuStatus status);

    boolean existsBySaamnaId(String saamnaId);
}
