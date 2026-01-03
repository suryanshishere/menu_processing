package com.shelfpulse.activation_automation.repository;

import com.shelfpulse.activation_automation.entity.ComboImage;
import com.shelfpulse.activation_automation.enums.DeletionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComboImageRepository extends JpaRepository<ComboImage, Integer> {

    long countByStatus(DeletionStatus status);

    Page<ComboImage> findByStatusAndImgNameContainingIgnoreCase(DeletionStatus status, String imgName,
            Pageable pageable);

    Page<ComboImage> findByStatusAndIsFavorite(DeletionStatus status, Boolean isFavorite, Pageable pageable);

    Page<ComboImage> findByStatusAndImgNameContainingIgnoreCaseAndIsFavorite(DeletionStatus status, String imgName,
            Boolean isFavorite, Pageable pageable);

    Page<ComboImage> findByStatus(DeletionStatus status, Pageable pageable);

    @Modifying
    @Query("UPDATE ComboImage c SET c.status = :status WHERE c.id IN :ids")
    void updateStatusByIds(@Param("ids") List<Integer> ids, @Param("status") DeletionStatus status);

    @Query("SELECT c FROM ComboImage c WHERE c.status = :status")
    List<ComboImage> findAllActiveWithInfo(@Param("status") DeletionStatus status);
}
