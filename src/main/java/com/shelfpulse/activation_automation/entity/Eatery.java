package com.shelfpulse.activation_automation.entity;

import com.shelfpulse.activation_automation.enums.MenuStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "eateries")
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

    @Convert(converter = com.shelfpulse.activation_automation.converter.MenuStatusConverter.class)
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSaamnaId() {
        return saamnaId;
    }

    public void setSaamnaId(String saamnaId) {
        this.saamnaId = saamnaId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public String getSubChannel() {
        return subChannel;
    }

    public void setSubChannel(String subChannel) {
        this.subChannel = subChannel;
    }

    public String getWebsiteLink() {
        return websiteLink;
    }

    public void setWebsiteLink(String websiteLink) {
        this.websiteLink = websiteLink;
    }

    public MenuStatus getStatus() {
        return status;
    }

    public void setStatus(MenuStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public Integer getMenusCreated() {
        return menusCreated;
    }

    public void setMenusCreated(Integer menusCreated) {
        this.menusCreated = menusCreated;
    }

    public String getEateryImgUrl() {
        return eateryImgUrl;
    }

    public void setEateryImgUrl(String eateryImgUrl) {
        this.eateryImgUrl = eateryImgUrl;
    }

    public String getEateryLogoImgUrl() {
        return eateryLogoImgUrl;
    }

    public void setEateryLogoImgUrl(String eateryLogoImgUrl) {
        this.eateryLogoImgUrl = eateryLogoImgUrl;
    }
}
