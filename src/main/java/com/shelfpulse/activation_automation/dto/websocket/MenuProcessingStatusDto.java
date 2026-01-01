package com.shelfpulse.activation_automation.dto.websocket;

import com.shelfpulse.activation_automation.enums.MenuStatus;
import com.shelfpulse.activation_automation.enums.OrientationType;

public class MenuProcessingStatusDto {
    private Long eateryId;
    private MenuStatus status;
    private String message;
    private Object data;
    private String dataJsonUrl;
    private OrientationType orientation;
    private Long templateId;

    public MenuProcessingStatusDto() {
    }

    public MenuProcessingStatusDto(Long eateryId, MenuStatus status, String message,
            Object data, String dataJsonUrl, OrientationType orientation, Long templateId) {
        this.eateryId = eateryId;
        this.status = status;
        this.message = message;
        this.data = data;
        this.dataJsonUrl = dataJsonUrl;
        this.orientation = orientation;
        this.templateId = templateId;
    }

    public Long getEateryId() {
        return eateryId;
    }

    public void setEateryId(Long eateryId) {
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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getDataJsonUrl() {
        return dataJsonUrl;
    }

    public void setDataJsonUrl(String dataJsonUrl) {
        this.dataJsonUrl = dataJsonUrl;
    }

    public OrientationType getOrientation() {
        return orientation;
    }

    public void setOrientation(OrientationType orientation) {
        this.orientation = orientation;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long eateryId;
        private MenuStatus status;
        private String message;
        private Object data;
        private String dataJsonUrl;
        private OrientationType orientation;
        private Long templateId;

        public Builder eateryId(Long eateryId) {
            this.eateryId = eateryId;
            return this;
        }

        public Builder status(MenuStatus status) {
            this.status = status;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder data(Object data) {
            this.data = data;
            return this;
        }

        public Builder dataJsonUrl(String dataJsonUrl) {
            this.dataJsonUrl = dataJsonUrl;
            return this;
        }

        public Builder orientation(OrientationType orientation) {
            this.orientation = orientation;
            return this;
        }

        public Builder templateId(Long templateId) {
            this.templateId = templateId;
            return this;
        }

        public MenuProcessingStatusDto build() {
            return new MenuProcessingStatusDto(eateryId, status, message, data, dataJsonUrl, orientation, templateId);
        }
    }
}
