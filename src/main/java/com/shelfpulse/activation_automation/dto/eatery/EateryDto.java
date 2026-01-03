package com.shelfpulse.activation_automation.dto.eatery;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public class EateryDto {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreateRequest {
        @NotBlank(message = "Eatery Name is required")
        private String name;

        @NotBlank(message = "SAAMNA ID is required")
        private String saamnaId;

        @NotBlank(message = "Address is required")
        @Size(min = 5, message = "Address must be at least 5 characters")
        private String address;

        @NotBlank(message = "State is required")
        private String state;

        @NotBlank(message = "City is required")
        private String city;

        @NotBlank(message = "Postal Code is required")
        @Pattern(regexp = "^\\d{4,6}$", message = "Postal Code must be 4–6 digits")
        private String postalCode;

        private String phoneNumber;

        @Email(message = "Invalid email format")
        private String email;

        private String websiteLink;

        private String subChannel;

        @DecimalMin(value = "-180", message = "Longitude must be greater than or equal to -180")
        @DecimalMax(value = "180", message = "Longitude must be less than or equal to 180")
        private BigDecimal longitude;

        @DecimalMin(value = "-90", message = "Latitude must be greater than or equal to -90")
        @DecimalMax(value = "90", message = "Latitude must be less than or equal to 90")
        private BigDecimal latitude;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name != null ? name.trim() : null;
        }

        public String getSaamnaId() {
            return saamnaId;
        }

        public void setSaamnaId(String saamnaId) {
            this.saamnaId = saamnaId != null ? saamnaId.trim() : null;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address != null ? address.trim() : null;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state != null ? state.trim() : null;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city != null ? city.trim() : null;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode != null ? postalCode.trim() : null;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = normalizePhoneNumber(phoneNumber);
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email != null && !email.isBlank() ? email.trim().toLowerCase() : null;
        }

        public String getWebsiteLink() {
            return websiteLink;
        }

        public void setWebsiteLink(String websiteLink) {
            this.websiteLink = normalizeWebsiteLink(websiteLink);
        }

        public String getSubChannel() {
            return subChannel;
        }

        public void setSubChannel(String subChannel) {
            this.subChannel = subChannel != null && !subChannel.isBlank() ? subChannel.trim() : null;
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
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UpdateRequest {
        @Size(min = 1, message = "Name cannot be empty")
        private String name;

        @Size(min = 1, message = "SAAMNA ID cannot be empty")
        private String saamnaId;

        @Email(message = "Must be a valid email address")
        private String email;

        private String phoneNumber;

        @Size(min = 5, message = "Address must be at least 5 characters")
        private String address;

        @Size(min = 1, message = "State cannot be empty")
        private String state;

        @Size(min = 1, message = "City cannot be empty")
        private String city;

        @Pattern(regexp = "^\\d{4,6}$", message = "Postal Code must be 4–6 digits")
        private String postalCode;

        @DecimalMin(value = "-180", message = "Longitude must be a valid number")
        @DecimalMax(value = "180", message = "Longitude must be a valid number")
        private BigDecimal longitude;

        @DecimalMin(value = "-90", message = "Latitude must be a valid number")
        @DecimalMax(value = "90", message = "Latitude must be a valid number")
        private BigDecimal latitude;

        @Size(min = 1, message = "Sub-Channel cannot be empty")
        private String subChannel;

        private String websiteLink;

        private Boolean removeEateryImage;

        private Boolean removeEateryLogoImage;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name != null && !name.isBlank() ? name.trim() : null;
        }

        public String getSaamnaId() {
            return saamnaId;
        }

        public void setSaamnaId(String saamnaId) {
            this.saamnaId = saamnaId != null && !saamnaId.isBlank() ? saamnaId.trim() : null;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email != null && !email.isBlank() ? email.trim().toLowerCase() : null;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = normalizePhoneNumber(phoneNumber);
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address != null && !address.isBlank() ? address.trim() : null;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state != null && !state.isBlank() ? state.trim() : null;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city != null && !city.isBlank() ? city.trim() : null;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode != null && !postalCode.isBlank() ? postalCode.trim() : null;
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
            this.subChannel = subChannel != null && !subChannel.isBlank() ? subChannel.trim() : null;
        }

        public String getWebsiteLink() {
            return websiteLink;
        }

        public void setWebsiteLink(String websiteLink) {
            this.websiteLink = normalizeWebsiteLink(websiteLink);
        }

        public Boolean getRemoveEateryImage() {
            return removeEateryImage;
        }

        public void setRemoveEateryImage(Boolean removeEateryImage) {
            this.removeEateryImage = removeEateryImage;
        }

        public Boolean getRemoveEateryLogoImage() {
            return removeEateryLogoImage;
        }

        public void setRemoveEateryLogoImage(Boolean removeEateryLogoImage) {
            this.removeEateryLogoImage = removeEateryLogoImage;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GetAllRequest {
        @Positive
        private Integer page = 1;

        @Positive
        private Integer limit = 10;

        private String search;

        private String sortBy = "creationDate";
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeleteRequest {
        @NotNull(message = "At least one Eatery ID must be provided")
        @Size(min = 1, message = "At least one Eatery ID must be provided")
        private List<@NotNull @Positive(message = "Eatery ID must be a positive number") Integer> eateryIds;

        public List<Integer> getEateryIds() {
            return eateryIds;
        }

        public void setEateryIds(List<Integer> eateryIds) {
            this.eateryIds = eateryIds;
        }
    }

    private static String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return null;
        }
        String trimmed = phoneNumber.trim();
        String digitsOnly = trimmed.replaceAll("[^0-9+]", "");

        if (digitsOnly.matches("^\\d{10}$")) {
            return "+91" + digitsOnly;
        }
        if (digitsOnly.matches("^91\\d{10}$")) {
            return "+" + digitsOnly;
        }
        if (digitsOnly.matches("^0\\d{10}$")) {
            return "+91" + digitsOnly.substring(1);
        }
        if (digitsOnly.matches("^\\+\\d{10,15}$")) {
            return digitsOnly;
        }
        return trimmed;
    }

    private static String normalizeWebsiteLink(String websiteLink) {
        if (websiteLink == null || websiteLink.isBlank()) {
            return null;
        }
        String trimmed = websiteLink.trim();
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            return "https://" + trimmed;
        }
        return trimmed;
    }
}
