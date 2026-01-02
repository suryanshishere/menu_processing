package com.shelfpulse.activation_automation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {

    private String mode;
    private String secretKey;
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

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

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
        private String type;
        private String projectId;
        private String bucketPrivateKeyId;
        private String bucketPrivateKey;
        private String clientEmail;
        private String clientId;
        private String authUri;
        private String tokenUri;
        private String authProviderX509CertUrl;
        private String clientX509CertUrl;
        private String universeDomain;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

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

        public String getClientEmail() {
            return clientEmail;
        }

        public void setClientEmail(String clientEmail) {
            this.clientEmail = clientEmail;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getAuthUri() {
            return authUri;
        }

        public void setAuthUri(String authUri) {
            this.authUri = authUri;
        }

        public String getTokenUri() {
            return tokenUri;
        }

        public void setTokenUri(String tokenUri) {
            this.tokenUri = tokenUri;
        }

        public String getAuthProviderX509CertUrl() {
            return authProviderX509CertUrl;
        }

        public void setAuthProviderX509CertUrl(String authProviderX509CertUrl) {
            this.authProviderX509CertUrl = authProviderX509CertUrl;
        }

        public String getClientX509CertUrl() {
            return clientX509CertUrl;
        }

        public void setClientX509CertUrl(String clientX509CertUrl) {
            this.clientX509CertUrl = clientX509CertUrl;
        }

        public String getUniverseDomain() {
            return universeDomain;
        }

        public void setUniverseDomain(String universeDomain) {
            this.universeDomain = universeDomain;
        }
    }
}
