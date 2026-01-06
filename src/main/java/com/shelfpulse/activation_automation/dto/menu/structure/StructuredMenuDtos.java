package com.shelfpulse.activation_automation.dto.menu.structure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class StructuredMenuDtos {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class StructuredMenu {
        private Map<String, MenuPage> pages;
        @JsonProperty("meta_data")
        private MetaData metaData;

        public Map<String, MenuPage> getPages() {
            return pages;
        }

        public void setPages(Map<String, MenuPage> pages) {
            this.pages = pages;
        }

        public MetaData getMetaData() {
            return metaData;
        }

        public void setMetaData(MetaData metaData) {
            this.metaData = metaData;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MenuPage {
        private List<FlattenedNode> data;
        @JsonProperty("column_data")
        private ColumnMetadata columnData;
        @JsonProperty("recommended_combo_img")
        private List<ComboImageDto> recommendedComboImg;

        public List<FlattenedNode> getData() {
            return data;
        }

        public void setData(List<FlattenedNode> data) {
            this.data = data;
        }

        public ColumnMetadata getColumnData() {
            return columnData;
        }

        public void setColumnData(ColumnMetadata columnData) {
            this.columnData = columnData;
        }

        public List<ComboImageDto> getRecommendedComboImg() {
            return recommendedComboImg;
        }

        public void setRecommendedComboImg(List<ComboImageDto> recommendedComboImg) {
            this.recommendedComboImg = recommendedComboImg;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FlattenedNode {
        private String id;
        private String type;
        private String imgUrl;
        private String title;
        private String name;
        private List<ItemPrice> prices;
        private String text;
        private RestaurantInfo value;

        public FlattenedNode() {
        }

        public FlattenedNode(String id, String type, String imgUrl, String title, String name, List<ItemPrice> prices,
                String text, RestaurantInfo value) {
            this.id = id;
            this.type = type;
            this.imgUrl = imgUrl;
            this.title = title;
            this.name = name;
            this.prices = prices;
            this.text = text;
            this.value = value;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getImgUrl() {
            return imgUrl;
        }

        public void setImgUrl(String imgUrl) {
            this.imgUrl = imgUrl;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<ItemPrice> getPrices() {
            return prices;
        }

        public void setPrices(List<ItemPrice> prices) {
            this.prices = prices;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public RestaurantInfo getValue() {
            return value;
        }

        public void setValue(RestaurantInfo value) {
            this.value = value;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ItemPrice {
        @JsonProperty("diet_type")
        private String dietType;
        private String portion;
        private String price;

        public ItemPrice() {
        }

        public ItemPrice(String dietType, String portion, String price) {
            this.dietType = dietType;
            this.portion = portion;
            this.price = price;
        }

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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MetaData {
        @JsonProperty("restaurant_info")
        private RestaurantInfo restaurantInfo;
        private String orientation;
        @JsonProperty("raw_img_urls")
        private List<String> rawImgUrls;
        private String size;

        public RestaurantInfo getRestaurantInfo() {
            return restaurantInfo;
        }

        public void setRestaurantInfo(RestaurantInfo restaurantInfo) {
            this.restaurantInfo = restaurantInfo;
        }

        public String getOrientation() {
            return orientation;
        }

        public void setOrientation(String orientation) {
            this.orientation = orientation;
        }

        public List<String> getRawImgUrls() {
            return rawImgUrls;
        }

        public void setRawImgUrls(List<String> rawImgUrls) {
            this.rawImgUrls = rawImgUrls;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RestaurantInfo {
        @JsonProperty("restaurant_name")
        private String restaurantName;
        private String address;
        private String hours;
        private ContactInfo contact;
        @JsonProperty("cuisine_type")
        private List<String> cuisineType;
        @JsonProperty("additional_info")
        private List<String> additionalInfo;

        public String getRestaurantName() {
            return restaurantName;
        }

        public void setRestaurantName(String restaurantName) {
            this.restaurantName = restaurantName;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getHours() {
            return hours;
        }

        public void setHours(String hours) {
            this.hours = hours;
        }

        public ContactInfo getContact() {
            return contact;
        }

        public void setContact(ContactInfo contact) {
            this.contact = contact;
        }

        public List<String> getCuisineType() {
            return cuisineType;
        }

        public void setCuisineType(List<String> cuisineType) {
            this.cuisineType = cuisineType;
        }

        public List<String> getAdditionalInfo() {
            return additionalInfo;
        }

        public void setAdditionalInfo(List<String> additionalInfo) {
            this.additionalInfo = additionalInfo;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ContactInfo {
        private String email;
        private String website;
        private List<String> phone;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getWebsite() {
            return website;
        }

        public void setWebsite(String website) {
            this.website = website;
        }

        public List<String> getPhone() {
            return phone;
        }

        public void setPhone(List<String> phone) {
            this.phone = phone;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ComboImageDto {
        @JsonProperty("img_url")
        private String imgUrl;
        @JsonProperty("compress_img_url")
        private String compressImgUrl;
        private List<String> info;
        @JsonProperty("section_title")
        private String sectionTitle;
        private String imgName;
        private Boolean isFavorite;

        public String getImgUrl() {
            return imgUrl;
        }

        public void setImgUrl(String imgUrl) {
            this.imgUrl = imgUrl;
        }

        public String getCompressImgUrl() {
            return compressImgUrl;
        }

        public void setCompressImgUrl(String compressImgUrl) {
            this.compressImgUrl = compressImgUrl;
        }

        public List<String> getInfo() {
            return info;
        }

        public void setInfo(List<String> info) {
            this.info = info;
        }

        public String getSectionTitle() {
            return sectionTitle;
        }

        public void setSectionTitle(String sectionTitle) {
            this.sectionTitle = sectionTitle;
        }

        public String getImgName() {
            return imgName;
        }

        public void setImgName(String imgName) {
            this.imgName = imgName;
        }

        public Boolean getIsFavorite() {
            return isFavorite;
        }

        public void setIsFavorite(Boolean favorite) {
            isFavorite = favorite;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ColumnMetadata {
        @JsonProperty("columns_count")
        private Integer columnsCount;
        @JsonProperty("total_items")
        private Integer totalItems;
        @JsonProperty("total_sections")
        private Integer totalSections;
        @JsonProperty("menu_sections_count")
        private Integer menuSectionsCount;
        private List<Integer> coordinates;

        public Integer getColumnsCount() {
            return columnsCount;
        }

        public void setColumnsCount(Integer columnsCount) {
            this.columnsCount = columnsCount;
        }

        public Integer getTotalItems() {
            return totalItems;
        }

        public void setTotalItems(Integer totalItems) {
            this.totalItems = totalItems;
        }

        public Integer getTotalSections() {
            return totalSections;
        }

        public void setTotalSections(Integer totalSections) {
            this.totalSections = totalSections;
        }

        public Integer getMenuSectionsCount() {
            return menuSectionsCount;
        }

        public void setMenuSectionsCount(Integer menuSectionsCount) {
            this.menuSectionsCount = menuSectionsCount;
        }

        public List<Integer> getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(List<Integer> coordinates) {
            this.coordinates = coordinates;
        }
    }
}
