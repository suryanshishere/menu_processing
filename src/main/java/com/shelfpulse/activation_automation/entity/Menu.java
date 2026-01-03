package com.shelfpulse.activation_automation.entity;

import com.shelfpulse.activation_automation.enums.MenuStatus;
import com.shelfpulse.activation_automation.enums.TemplateType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menus")
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eatery_id", nullable = false)
    private Eatery eatery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "raw_menu_img_urls", nullable = false)
    private List<String> rawMenuImgUrls = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MenuStatus status = MenuStatus.PROCESSING;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "data_json_url")
    private String dataJsonUrl;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "working_data_json_urls", nullable = false)
    private List<String> workingDataJsonUrls = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "pdf_urls", nullable = false)
    private List<String> pdfUrls = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "template_img_urls", nullable = false)
    private List<String> templateImgUrls = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "template_type")
    private TemplateType templateType;

    @Column(name = "template_name")
    private String templateName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Eatery getEatery() {
        return eatery;
    }

    public void setEatery(Eatery eatery) {
        this.eatery = eatery;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public List<String> getRawMenuImgUrls() {
        return rawMenuImgUrls;
    }

    public void setRawMenuImgUrls(List<String> rawMenuImgUrls) {
        this.rawMenuImgUrls = rawMenuImgUrls;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public MenuStatus getStatus() {
        return status;
    }

    public void setStatus(MenuStatus status) {
        this.status = status;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDataJsonUrl() {
        return dataJsonUrl;
    }

    public void setDataJsonUrl(String dataJsonUrl) {
        this.dataJsonUrl = dataJsonUrl;
    }

    public List<String> getWorkingDataJsonUrls() {
        return workingDataJsonUrls;
    }

    public void setWorkingDataJsonUrls(List<String> workingDataJsonUrls) {
        this.workingDataJsonUrls = workingDataJsonUrls;
    }

    public List<String> getPdfUrls() {
        return pdfUrls;
    }

    public void setPdfUrls(List<String> pdfUrls) {
        this.pdfUrls = pdfUrls;
    }

    public List<String> getTemplateImgUrls() {
        return templateImgUrls;
    }

    public void setTemplateImgUrls(List<String> templateImgUrls) {
        this.templateImgUrls = templateImgUrls;
    }

    public TemplateType getTemplateType() {
        return templateType;
    }

    public void setTemplateType(TemplateType templateType) {
        this.templateType = templateType;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
}
