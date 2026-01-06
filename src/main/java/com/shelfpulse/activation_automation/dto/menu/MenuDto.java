package com.shelfpulse.activation_automation.dto.menu;

import com.shelfpulse.activation_automation.enums.MenuStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public class MenuDto {

    public static class ProcessedMenuRequest {
        @NotEmpty(message = "At least one JSON URL is required")
        private List<@NotNull String> jsonUrls;

        public List<String> getJsonUrls() {
            return jsonUrls;
        }

        public void setJsonUrls(List<String> jsonUrls) {
            this.jsonUrls = jsonUrls;
        }
    }

    public static class UploadMenuImagesRequest {
        @NotNull(message = "Eatery ID is required")
        @Positive(message = "Eatery ID must be a positive integer")
        private Integer eateryId;

        public Integer getEateryId() {
            return eateryId;
        }

        public void setEateryId(Integer eateryId) {
            this.eateryId = eateryId;
        }
    }

    public static class MenuStatusResponse {
        private Integer eateryId;
        private MenuStatus status;
        private String message;
        private String dataJsonUrl;
        private Integer templateId;

        public MenuStatusResponse() {
        }

        public MenuStatusResponse(Integer eateryId, MenuStatus status, String message) {
            this.eateryId = eateryId;
            this.status = status;
            this.message = message;
        }

        public Integer getEateryId() {
            return eateryId;
        }

        public void setEateryId(Integer eateryId) {
            this.eateryId = eateryId;
        }

        public MenuStatus getStatus() {
            return status;
        }

        public void setStatus(MenuStatus status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getDataJsonUrl() {
            return dataJsonUrl;
        }

        public void setDataJsonUrl(String dataJsonUrl) {
            this.dataJsonUrl = dataJsonUrl;
        }

        public Integer getTemplateId() {
            return templateId;
        }

        public void setTemplateId(Integer templateId) {
            this.templateId = templateId;
        }
    }

    public static class MenuImageInfo {
        private String id;
        private String side;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSide() {
            return side;
        }

        public void setSide(String side) {
            this.side = side;
        }
    }
}
