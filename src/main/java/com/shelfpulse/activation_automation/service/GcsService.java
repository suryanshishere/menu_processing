package com.shelfpulse.activation_automation.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.shelfpulse.activation_automation.config.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class GcsService {

    private static final Logger log = LoggerFactory.getLogger(GcsService.class);

    private final ApplicationProperties applicationProperties;

    public GcsService(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    private Storage storage;
    private final String bucketName = "shelfex-cdn";

    @PostConstruct
    public void init() {
        try {
            ApplicationProperties.Gcp gcp = applicationProperties.getGcp();
            if (gcp == null) {
                log.warn("GCP configuration is missing - GCS service will not be available");
                return;
            }

            if (gcp.getProjectId() == null || gcp.getBucketPrivateKey() == null || gcp.getClientEmail() == null) {
                log.warn("GCP credentials are incomplete - GCS service will not be available. " +
                        "projectId={}, bucketPrivateKey={}, clientEmail={}",
                        gcp.getProjectId() != null ? "SET" : "NULL",
                        gcp.getBucketPrivateKey() != null ? "SET (length=" + gcp.getBucketPrivateKey().length() + ")"
                                : "NULL",
                        gcp.getClientEmail() != null ? "SET" : "NULL");
                return;
            }

            String jsonKey = String.format("{\n" +
                    "  \"type\": \"%s\",\n" +
                    "  \"project_id\": \"%s\",\n" +
                    "  \"private_key_id\": \"%s\",\n" +
                    "  \"private_key\": \"%s\",\n" +
                    "  \"client_email\": \"%s\",\n" +
                    "  \"client_id\": \"%s\",\n" +
                    "  \"auth_uri\": \"%s\",\n" +
                    "  \"token_uri\": \"%s\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"%s\",\n" +
                    "  \"client_x509_cert_url\": \"%s\",\n" +
                    "  \"universe_domain\": \"%s\"\n" +
                    "}",
                    gcp.getType() != null ? gcp.getType() : "service_account",
                    gcp.getProjectId(),
                    gcp.getBucketPrivateKeyId() != null ? gcp.getBucketPrivateKeyId() : "",
                    gcp.getBucketPrivateKey().replace("\n", "\\n"),
                    gcp.getClientEmail(),
                    gcp.getClientId() != null ? gcp.getClientId() : "",
                    gcp.getAuthUri() != null ? gcp.getAuthUri() : "https://accounts.google.com/o/oauth2/auth",
                    gcp.getTokenUri() != null ? gcp.getTokenUri() : "https://oauth2.googleapis.com/token",
                    gcp.getAuthProviderX509CertUrl() != null ? gcp.getAuthProviderX509CertUrl()
                            : "https://www.googleapis.com/oauth2/v1/certs",
                    gcp.getClientX509CertUrl() != null ? gcp.getClientX509CertUrl() : "",
                    gcp.getUniverseDomain() != null ? gcp.getUniverseDomain() : "googleapis.com");

            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(jsonKey.getBytes(StandardCharsets.UTF_8)));

            this.storage = StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .setProjectId(gcp.getProjectId())
                    .build()
                    .getService();

            log.info("GCS Storage initialized successfully for project: {}", gcp.getProjectId());

        } catch (Exception e) {
            log.error("Failed to initialize GCS Storage: {}", e.getMessage(), e);
            this.storage = null;
        }
    }

    private void uploadToGCS(String bucketName, byte[] content, String destinationBlobName, String contentType) {
        BlobId blobId = BlobId.of(bucketName, destinationBlobName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .setMetadata(Collections.singletonMap("cacheControl", "no-cache, max-age=0"))
                .build();
        storage.create(blobInfo, content);
    }

    public String uploadAdminRecognizedImage(Long adminId, Object filePath, String subdirectory, String fileName,
            String fileExtension) {
        if (storage == null) {
            throw new RuntimeException(
                    "GCS Storage is not initialized. Please check GCP configuration in application.properties");
        }
        try {
            byte[] imageBuffer;
            String finalFileExtension = fileExtension != null ? fileExtension : ".jpg";

            if (filePath instanceof byte[]) {
                imageBuffer = (byte[]) filePath;
            } else if (filePath instanceof String) {
                String pathStr = (String) filePath;
                if (pathStr.startsWith("http")) {
                    if (fileExtension == null) {
                        String ext = pathStr.substring(pathStr.lastIndexOf(".") + 1);
                        finalFileExtension = "." + ext;
                    }
                    try (InputStream in = java.net.URI.create(pathStr).toURL().openStream()) {
                        imageBuffer = in.readAllBytes();
                    }
                } else {
                    imageBuffer = Files.readAllBytes(Paths.get(pathStr));
                }
            } else {
                throw new IllegalArgumentException("Unsupported filePath format");
            }

            String uniqueId = String.valueOf(System.currentTimeMillis());
            String bucketDirName = "automation-activation";
            String imageName;

            if (StringUtils.hasText(fileName)) {
                imageName = fileName;
            } else {
                imageName = uniqueId + finalFileExtension;
            }

            String destinationBlobName = bucketDirName + "/" + adminId + "/" + subdirectory + "/" + imageName;

            BlobId blobId = BlobId.of(bucketName, destinationBlobName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType("image/jpeg") // Assuming logic; inferred from extension ideally
                    .setMetadata(Collections.singletonMap("cacheControl", "no-cache, max-age=0"))
                    .build();

            storage.create(blobInfo, imageBuffer);

            return "https://storage.googleapis.com/" + bucketName + "/" + destinationBlobName;

        } catch (Exception e) {
            log.error("error in uploading image to GCS: {}", e.getMessage());
            throw new RuntimeException("error in uploading image to GCS: " + e.getMessage());
        }
    }

    public String uploadJsonToGCS(Long adminId, String subdirectory, Object data, String filename) {
        try {
            String destinationBlobName = "automation-activation/" + adminId + "/" + subdirectory + "/" + filename
                    + ".json";

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            BlobId blobId = BlobId.of(bucketName, destinationBlobName);
            String contentType = "application/json";

            if (data instanceof List && ((List<?>) data).size() > 1000) {
                contentType = "application/jsonl";
            }

            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(contentType)
                    .build();

            // Use writer for streaming
            try (com.google.cloud.WriteChannel writer = storage.writer(blobInfo)) {
                if ("application/jsonl".equals(contentType)) {
                    for (Object item : (List<?>) data) {
                        String line = mapper.writeValueAsString(item) + "\n";
                        writer.write(java.nio.ByteBuffer.wrap(line.getBytes(StandardCharsets.UTF_8)));
                    }
                } else {
                    byte[] jsonBytes = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(data);
                    writer.write(java.nio.ByteBuffer.wrap(jsonBytes));
                }
            }

            return "https://storage.googleapis.com/" + bucketName + "/" + destinationBlobName;

        } catch (Exception e) {
            throw new RuntimeException("failed to upload JSON to GCS. Reason: " + e.getMessage());
        }
    }

    public String uploadFileToGCS(Object adminId, String subdirectory, Object filePath, String filename) {
        try {
            String destinationBlobName = "automation-activation/" + adminId + "/" + subdirectory + "/" + filename;
            BlobId blobId = BlobId.of(bucketName, destinationBlobName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

            if (filePath instanceof byte[]) {
                storage.create(blobInfo, (byte[]) filePath);
            } else if (filePath instanceof String) {
                // Stream file content instead of loading fully into memory
                try (com.google.cloud.WriteChannel writer = storage.writer(blobInfo);
                        InputStream inputStream = Files.newInputStream(Paths.get((String) filePath))) {
                    byte[] buffer = new byte[1024 * 1024]; // 1MB buffer
                    int limit;
                    while ((limit = inputStream.read(buffer)) >= 0) {
                        try {
                            writer.write(java.nio.ByteBuffer.wrap(buffer, 0, limit));
                        } catch (Exception ex) {
                            // Handle write errors
                            throw ex;
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("Unsupported filePath type");
            }

            return "https://storage.googleapis.com/" + bucketName + "/" + destinationBlobName;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> softDeleteGCSFile(List<String> gcsUrls) {
        List<String> newUrls = new ArrayList<>();
        for (String gcsUrl : gcsUrls) {
            try {
                // Parse URL
                URL url = java.net.URI.create(gcsUrl).toURL();
                String path = url.getPath();
                // strip leading slash
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }

                // path is bucketName/objectPath
                // The Node code expects path inside bucket.
                // The node code: filePath = urlParts.pathname.slice(1); if
                // (!filePath.startsWith(bucketName)) ...
                // gcsUrl: https://storage.googleapis.com/shelfex-cdn/automation-activation/...
                // path: shelfex-cdn/automation-activation/...

                if (!path.startsWith(bucketName + "/")) {
                    throw new IllegalArgumentException("invalid bucket in URL: " + gcsUrl);
                }

                String objectName = path.substring(bucketName.length() + 1); // remove bucketName + /

                if (!objectName.startsWith("automation-activation/")) {
                    throw new IllegalArgumentException("file is not inside automation-activation/: " + objectName);
                }

                if (objectName.endsWith("/")) {
                    throw new IllegalArgumentException("refusing to soft-delete a folder: " + objectName);
                }

                // Construct deleted path
                int lastSlashIndex = objectName.lastIndexOf("/");
                String parentDir = objectName.substring(0, lastSlashIndex);
                String fileName = objectName.substring(lastSlashIndex + 1);
                String deletedObjectName = parentDir + "/deleted/" + fileName;

                Blob sourceBlob = storage.get(bucketName, objectName);
                if (sourceBlob == null) {
                    log.warn("source file not found, it might have been deleted already: {}", gcsUrl);
                    continue;
                }

                // Move (Copy + Delete) or just Copy? Node used file.move()
                // Storage client copy
                Storage.CopyRequest copyRequest = Storage.CopyRequest.newBuilder()
                        .setSource(BlobId.of(bucketName, objectName))
                        .setTarget(BlobId.of(bucketName, deletedObjectName))
                        .build();
                Blob newBlob = storage.copy(copyRequest).getResult();

                // Update metadata
                newBlob.toBuilder().setMetadata(null) // clear if needed or just set
                        .setCacheControl("no-cache, no-store, max-age=0, must-revalidate")
                        .build()
                        .update();

                // Delete original
                sourceBlob.delete();

                String newUrl = "https://storage.googleapis.com/" + bucketName + "/" + deletedObjectName;
                newUrls.add(newUrl);
                log.info("moved {} -> {} with no-cache headers.", objectName, deletedObjectName);

            } catch (Exception e) {
                log.error("failed to soft delete {}: {}", gcsUrl, e.getMessage());
            }
        }
        return newUrls;
    }

    public Object fetchJsonFromGCS(String gcsUrl) {
        try {
            String prefix = "https://storage.googleapis.com/";
            if (!gcsUrl.startsWith(prefix)) {
                throw new IllegalArgumentException("Invalid GCS URL format.");
            }

            String pathWithBucket = gcsUrl.substring(prefix.length());
            int firstSlashIndex = pathWithBucket.indexOf("/");

            if (firstSlashIndex == -1) {
                throw new IllegalArgumentException("Invalid GCS URL format.");
            }

            String bucketName = pathWithBucket.substring(0, firstSlashIndex);
            String objectName = pathWithBucket.substring(firstSlashIndex + 1);

            Blob blob = storage.get(bucketName, objectName);
            if (blob == null) {
                throw new RuntimeException("File not found at GCS URL: " + gcsUrl);
            }

            byte[] content = blob.getContent();
            String jsonString = new String(content, StandardCharsets.UTF_8);

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(jsonString, Object.class);

        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred while fetching JSON from GCS: " + e.getMessage(),
                    e);
        }
    }
}
