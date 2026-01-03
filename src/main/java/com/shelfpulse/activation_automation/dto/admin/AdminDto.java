package com.shelfpulse.activation_automation.dto.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import java.util.List;

public class AdminDto {

    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class UpdateRequest {
        @Email(message = "Please provide a valid email address.")
        private String email;

        @Size(min = 3, message = "Username must be at least 3 characters long.")
        private String username;

        @Size(min = 1, message = "First name cannot be empty.")
        private String firstName;

        @Size(min = 1, message = "Last name cannot be empty.")
        private String lastName;

        @Pattern(regexp = "^\\+?[0-9-]{7,20}$", message = "Please provide a valid phone number format.")
        private String phoneNumber;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email != null ? email.toLowerCase() : null;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class UploadComboImageRequest {
        @Size(min = 3, message = "Image name must be at least 3 characters.")
        private String imgName;

        private Boolean isFavorite;

        private List<String> info;

        public String getImgName() {
            return imgName;
        }

        public void setImgName(String imgName) {
            this.imgName = imgName;
        }

        public Boolean getIsFavorite() {
            return isFavorite;
        }

        public void setIsFavorite(Boolean isFavorite) {
            this.isFavorite = isFavorite;
        }

        public List<String> getInfo() {
            return info;
        }

        public void setInfo(List<String> info) {
            this.info = info;
        }
    }

    public static class GetComboImagesRequest {
        @Positive
        private Integer page = 1;

        @Positive
        private Integer limit = 10;

        private String search;

        private Boolean isFavorite;

        private String sortBy = "uploadedAt";

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

        public Boolean getIsFavorite() {
            return isFavorite;
        }

        public void setIsFavorite(Boolean isFavorite) {
            this.isFavorite = isFavorite;
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

    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class DeleteComboImageRequest {
        @NotNull(message = "comboImageIds array is required.")
        @Size(min = 1, message = "At least one comboImageId must be provided.")
        private List<@NotNull @Positive(message = "Each ID must be a positive number.") Integer> comboImageIds;

        public List<Integer> getComboImageIds() {
            return comboImageIds;
        }

        public void setComboImageIds(List<Integer> comboImageIds) {
            this.comboImageIds = comboImageIds;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class UpdateComboImageRequest {
        private String imgName;
        private Boolean isFavorite;
        private List<String> info;

        public String getImgName() {
            return imgName;
        }

        public void setImgName(String imgName) {
            this.imgName = imgName;
        }

        public Boolean getIsFavorite() {
            return isFavorite;
        }

        public void setIsFavorite(Boolean isFavorite) {
            this.isFavorite = isFavorite;
        }

        public List<String> getInfo() {
            return info;
        }

        public void setInfo(List<String> info) {
            this.info = info;
        }

        private com.shelfpulse.activation_automation.enums.DeletionStatus status;

        public com.shelfpulse.activation_automation.enums.DeletionStatus getStatus() {
            return status;
        }

        public void setStatus(com.shelfpulse.activation_automation.enums.DeletionStatus status) {
            this.status = status;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class CreateAdminRequest {
        @Email(message = "Please provide a valid email address.")
        @NotNull(message = "Email is required.")
        private String email;

        @Size(min = 3, message = "Username must be at least 3 characters long.")
        @NotNull(message = "Username is required.")
        private String username;

        @Size(min = 6, message = "Password must be at least 6 characters long.")
        @NotNull(message = "Password is required.")
        private String password;

        @Size(min = 1, message = "First name is required.")
        @NotNull(message = "First name is required.")
        private String firstName;

        private String lastName;

        @Pattern(regexp = "^\\+?[0-9-]{7,20}$", message = "Please provide a valid phone number format.")
        private String phoneNumber;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email != null ? email.toLowerCase() : null;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }

    public static class HomeStatsResponse {
        private long totalEateries;
        private long completedMenus;
        private long generatedMenus;
        private long pendingMenus;

        public HomeStatsResponse(long totalEateries, long completedMenus, long generatedMenus, long pendingMenus) {
            this.totalEateries = totalEateries;
            this.completedMenus = completedMenus;
            this.generatedMenus = generatedMenus;
            this.pendingMenus = pendingMenus;
        }

        public long getTotalEateries() {
            return totalEateries;
        }

        public long getCompletedMenus() {
            return completedMenus;
        }

        public long getGeneratedMenus() {
            return generatedMenus;
        }

        public long getPendingMenus() {
            return pendingMenus;
        }
    }

    public static class PaginationInfo {
        private long total;
        private int page;
        private int limit;
        private int totalPages;

        public PaginationInfo(long total, int page, int limit) {
            this.total = total;
            this.page = page;
            this.limit = limit;
            this.totalPages = (int) Math.ceil((double) total / limit);
        }

        public long getTotal() {
            return total;
        }

        public int getPage() {
            return page;
        }

        public int getLimit() {
            return limit;
        }

        public int getTotalPages() {
            return totalPages;
        }
    }
}
