package com.shelfpulse.activation_automation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {

    private String envMode;
    private Jwt jwt = new Jwt();
    private String bcryptCostFactor;
    private Azure azure = new Azure();
    private Email email = new Email();
    private Gcp gcp = new Gcp();
    private String aiBackendUrl;
    private String aiBackendSecretKey;
    private String defaultGcsUrl;
    private String defaultProfileImgUrl;
    private String defaultAdminId;
    private String frontendUrl;
    private String goBackendUrl;
    private String redisUrlDev;
    private String redisUrlProd;

    public String getEnvMode() {
        return envMode;
    }

    public void setEnvMode(String envMode) {
        this.envMode = envMode;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public String getBcryptCostFactor() {
        return bcryptCostFactor;
    }

    public void setBcryptCostFactor(String bcryptCostFactor) {
        this.bcryptCostFactor = bcryptCostFactor;
    }

    public Azure getAzure() {
        return azure;
    }

    public void setAzure(Azure azure) {
        this.azure = azure;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public Gcp getGcp() {
        return gcp;
    }

    public void setGcp(Gcp gcp) {
        this.gcp = gcp;
    }

    public String getAiBackendUrl() {
        return aiBackendUrl;
    }

    public void setAiBackendUrl(String aiBackendUrl) {
        this.aiBackendUrl = aiBackendUrl;
    }

    public String getAiBackendSecretKey() {
        return aiBackendSecretKey;
    }

    public void setAiBackendSecretKey(String aiBackendSecretKey) {
        this.aiBackendSecretKey = aiBackendSecretKey;
    }

    public String getDefaultGcsUrl() {
        return defaultGcsUrl;
    }

    public void setDefaultGcsUrl(String defaultGcsUrl) {
        this.defaultGcsUrl = defaultGcsUrl;
    }

    public String getDefaultProfileImgUrl() {
        return defaultProfileImgUrl;
    }

    public void setDefaultProfileImgUrl(String defaultProfileImgUrl) {
        this.defaultProfileImgUrl = defaultProfileImgUrl;
    }

    public String getDefaultAdminId() {
        return defaultAdminId;
    }

    public void setDefaultAdminId(String defaultAdminId) {
        this.defaultAdminId = defaultAdminId;
    }

    public String getFrontendUrl() {
        return frontendUrl;
    }

    public void setFrontendUrl(String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    public String getGoBackendUrl() {
        return goBackendUrl;
    }

    public void setGoBackendUrl(String goBackendUrl) {
        this.goBackendUrl = goBackendUrl;
    }

    public String getRedisUrlDev() {
        return redisUrlDev;
    }

    public void setRedisUrlDev(String redisUrlDev) {
        this.redisUrlDev = redisUrlDev;
    }

    public String getRedisUrlProd() {
        return redisUrlProd;
    }

    public void setRedisUrlProd(String redisUrlProd) {
        this.redisUrlProd = redisUrlProd;
    }

    public static class Jwt {
        private String secret;
        private long accessExpirationMinutes;
        private long refreshExpirationDays;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getAccessExpirationMinutes() {
            return accessExpirationMinutes;
        }

        public void setAccessExpirationMinutes(long accessExpirationMinutes) {
            this.accessExpirationMinutes = accessExpirationMinutes;
        }

        public long getRefreshExpirationDays() {
            return refreshExpirationDays;
        }

        public void setRefreshExpirationDays(long refreshExpirationDays) {
            this.refreshExpirationDays = refreshExpirationDays;
        }
    }

    public static class Azure {
        private String accountName;
        private String accountKeyPart1;
        private String accountKeyPart2;
        private String endpointSuffix;
        private String defaultEndpointsProtocol;

        public String getAccountName() {
            return accountName;
        }

        public void setAccountName(String accountName) {
            this.accountName = accountName;
        }

        public String getAccountKeyPart1() {
            return accountKeyPart1;
        }

        public void setAccountKeyPart1(String accountKeyPart1) {
            this.accountKeyPart1 = accountKeyPart1;
        }

        public String getAccountKeyPart2() {
            return accountKeyPart2;
        }

        public void setAccountKeyPart2(String accountKeyPart2) {
            this.accountKeyPart2 = accountKeyPart2;
        }

        public String getEndpointSuffix() {
            return endpointSuffix;
        }

        public void setEndpointSuffix(String endpointSuffix) {
            this.endpointSuffix = endpointSuffix;
        }

        public String getDefaultEndpointsProtocol() {
            return defaultEndpointsProtocol;
        }

        public void setDefaultEndpointsProtocol(String defaultEndpointsProtocol) {
            this.defaultEndpointsProtocol = defaultEndpointsProtocol;
        }
    }

    public static class Email {
        private String user;
        private String pass;

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPass() {
            return pass;
        }

        public void setPass(String pass) {
            this.pass = pass;
        }
    }

    public static class Gcp {
        private String bucketPrivateKeyId;
        private String bucketPrivateKey;

        public String getBucketPrivateKeyId() {
            return bucketPrivateKeyId;
        }

        public void setBucketPrivateKeyId(String bucketPrivateKeyId) {
            this.bucketPrivateKeyId = bucketPrivateKeyId;
        }

        public String getBucketPrivateKey() {
            return bucketPrivateKey;
        }

        public void setBucketPrivateKey(String bucketPrivateKey) {
            this.bucketPrivateKey = bucketPrivateKey;
        }
    }
}
