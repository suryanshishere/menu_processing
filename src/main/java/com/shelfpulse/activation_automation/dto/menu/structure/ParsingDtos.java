package com.shelfpulse.activation_automation.dto.menu.structure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class ParsingDtos {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MenuJsonData {
        @JsonProperty("processed_files")
        private Map<String, ProcessedFile> processedFiles;

        public Map<String, ProcessedFile> getProcessedFiles() {
            return processedFiles;
        }

        public void setProcessedFiles(Map<String, ProcessedFile> processedFiles) {
            this.processedFiles = processedFiles;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProcessedFile {
        @JsonProperty("column_metadata")
        private StructuredMenuDtos.ColumnMetadata columnMetadata;

        @JsonProperty("orientation_info")
        private OrientationInfo orientationInfo;

        public StructuredMenuDtos.ColumnMetadata getColumnMetadata() {
            return columnMetadata;
        }

        public void setColumnMetadata(StructuredMenuDtos.ColumnMetadata columnMetadata) {
            this.columnMetadata = columnMetadata;
        }

        public OrientationInfo getOrientationInfo() {
            return orientationInfo;
        }

        public void setOrientationInfo(OrientationInfo orientationInfo) {
            this.orientationInfo = orientationInfo;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrientationInfo {
        @JsonProperty("original_orientation")
        private String originalOrientation;

        @JsonProperty("corrected_orientation")
        private String correctedOrientation;

        public String getOriginalOrientation() {
            return originalOrientation;
        }

        public void setOriginalOrientation(String originalOrientation) {
            this.originalOrientation = originalOrientation;
        }

        public String getCorrectedOrientation() {
            return correctedOrientation;
        }

        public void setCorrectedOrientation(String correctedOrientation) {
            this.correctedOrientation = correctedOrientation;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MenuContent {
        @JsonProperty("restaurant_info")
        private StructuredMenuDtos.RestaurantInfo restaurantInfo;

        private List<MenuSection> sections;

        public StructuredMenuDtos.RestaurantInfo getRestaurantInfo() {
            return restaurantInfo;
        }

        public void setRestaurantInfo(StructuredMenuDtos.RestaurantInfo restaurantInfo) {
            this.restaurantInfo = restaurantInfo;
        }

        public List<MenuSection> getSections() {
            return sections;
        }

        public void setSections(List<MenuSection> sections) {
            this.sections = sections;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MenuSection {
        private String title;
        private String note;
        private List<MenuItem> items;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public List<MenuItem> getItems() {
            return items;
        }

        public void setItems(List<MenuItem> items) {
            this.items = items;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MenuItem {
        private String name;
        private String description;
        @JsonProperty("dietary_info")
        private List<String> dietaryInfoList;
        private List<String> dietaryInfo;
        private List<ItemPriceRow> prices;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getDietaryInfoList() {
            return dietaryInfoList;
        }

        public void setDietaryInfoList(List<String> dietaryInfoList) {
            this.dietaryInfoList = dietaryInfoList;
        }

        public List<String> getDietaryInfo() {
            return dietaryInfo;
        }

        public void setDietaryInfo(List<String> dietaryInfo) {
            this.dietaryInfo = dietaryInfo;
        }

        public List<ItemPriceRow> getPrices() {
            return prices;
        }

        public void setPrices(List<ItemPriceRow> prices) {
            this.prices = prices;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ItemPriceRow {
        @JsonProperty("diet_type")
        private String dietType;
        private String portion;
        private String price;

        public String getDietType() {
            return dietType;
        }

        public void setDietType(String dietType) {
            this.dietType = dietType;
        }

        public String getPortion() {
            return portion;
        }

        public void setPortion(String portion) {
            this.portion = portion;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }
    }
}
