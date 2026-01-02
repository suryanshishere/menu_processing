package com.shelfpulse.activation_automation.entity;

import com.shelfpulse.activation_automation.enums.DeletionStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "combo_images")
public class ComboImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "img_name")
    private String imgName;

    @Column(name = "is_favorite", nullable = false)
    private Boolean isFavorite = false;

    @Column(name = "img_url", nullable = false)
    private String imgUrl;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(nullable = false)
    private List<String> info = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeletionStatus status = DeletionStatus.ACTIVE;

    @Column(name = "compress_img_url", columnDefinition = "TEXT")
    private String compressImgUrl;
}
