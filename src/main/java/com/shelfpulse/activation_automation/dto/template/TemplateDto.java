package com.shelfpulse.activation_automation.dto.template;

import com.shelfpulse.activation_automation.enums.TemplateType;
import jakarta.validation.constraints.Positive;

public class TemplateDto {

    public static class UpdateTemplateRequest {
        private String templateName;
        private Object data;
        private TemplateType type;
        private Boolean generatePdf = true;

        public String getTemplateName() {
            return templateName;
        }

        public void setTemplateName(String templateName) {
            this.templateName = templateName;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }

        public TemplateType getType() {
            return type;
        }

        public void setType(TemplateType type) {
            this.type = type;
        }

        public Boolean getGeneratePdf() {
            return generatePdf;
        }

        public void setGeneratePdf(Boolean generatePdf) {
            this.generatePdf = generatePdf;
        }
    }

    public static class GetTemplateRequest {
        @Positive(message = "Page must be a positive integer")
        private Integer page = 1;

        @Positive(message = "Limit must be a positive integer")
        private Integer limit = 10;

        private String search;
        private String sortBy = "createdAt";
        private String sortOrder = "desc";

        public Integer getPage() {
            return page;
        }

        public void setPage(Integer page) {
            this.page = page;
        }

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }

        public String getSearch() {
            return search;
        }

        public void setSearch(String search) {
            this.search = search;
        }

        public String getSortBy() {
            return sortBy;
        }

        public void setSortBy(String sortBy) {
            this.sortBy = sortBy;
        }

        public String getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(String sortOrder) {
            this.sortOrder = sortOrder;
        }
    }

    public static class TemplateListResponse {
        private Integer id;
        private Integer eateryId;
        private String createdAt;
        private String updatedAt;
        private String status;
        private String templateName;
        private Object imageUrls;
        private Object rawMenuImageUrls;
        private String type;
        private String initialJsonUrl;
        private String jsonUrl;
        private String pdfUrl;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Integer getEateryId() {
            return eateryId;
        }

        public void setEateryId(Integer eateryId) {
            this.eateryId = eateryId;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getTemplateName() {
            return templateName;
        }

        public void setTemplateName(String templateName) {
            this.templateName = templateName;
        }

        public Object getImageUrls() {
            return imageUrls;
        }

        public void setImageUrls(Object imageUrls) {
            this.imageUrls = imageUrls;
        }

        public Object getRawMenuImageUrls() {
            return rawMenuImageUrls;
        }

        public void setRawMenuImageUrls(Object rawMenuImageUrls) {
            this.rawMenuImageUrls = rawMenuImageUrls;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getInitialJsonUrl() {
            return initialJsonUrl;
        }

        public void setInitialJsonUrl(String initialJsonUrl) {
            this.initialJsonUrl = initialJsonUrl;
        }

        public String getJsonUrl() {
            return jsonUrl;
        }

        public void setJsonUrl(String jsonUrl) {
            this.jsonUrl = jsonUrl;
        }

        public String getPdfUrl() {
            return pdfUrl;
        }

        public void setPdfUrl(String pdfUrl) {
            this.pdfUrl = pdfUrl;
        }
    }
}
