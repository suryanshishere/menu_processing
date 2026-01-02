package com.shelfpulse.activation_automation.entity;

import com.shelfpulse.activation_automation.enums.MenuStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "eateries", uniqueConstraints = {
        @UniqueConstraint(columnNames = "saamna_id"),
        @UniqueConstraint(columnNames = "email")
})
public class Eatery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @Column(nullable = false)
    private String name;

    @Column(name = "saamna_id", nullable = false)
    private String saamnaId;

    @Column(length = 320)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String city;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(precision = 8, scale = 6)
    private BigDecimal latitude;

    @Column(name = "sub_channel")
    private String subChannel;

    @Column(name = "website_link")
    private String websiteLink;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MenuStatus status = MenuStatus.COMPLETED;

    @CreationTimestamp
    @Column(name = "creation_date", updatable = false)
    private LocalDateTime creationDate;

    @Column(name = "menus_created", nullable = false)
    private Integer menusCreated = 0;

    @Column(name = "eatery_img_url")
    private String eateryImgUrl;

    @Column(name = "eatery_logo_img_url")
    private String eateryLogoImgUrl;
}
