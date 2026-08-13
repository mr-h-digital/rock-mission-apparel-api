package co.za.rockmission.apparelapi.storage;

import co.za.rockmission.apparelapi.common.BadRequestException;
import co.za.rockmission.apparelapi.config.StorageProperties;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class StorageService {

    private final StorageProperties storageProperties;
    private final S3Client s3Client;

    public StorageService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
        this.s3Client = buildClient(storageProperties);
    }

    public UploadedFile uploadProductImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please choose an image file to upload.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BadRequestException("Only image files can be uploaded.");
        }

        String objectKey = buildObjectKey(file);

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(requireConfigured(storageProperties.bucketName(), "STORAGE_BUCKET_NAME"))
                            .key(objectKey)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException ex) {
            throw new BadRequestException("Unable to read the uploaded image file.");
        }

        return new UploadedFile(buildPublicUrl(objectKey), objectKey);
    }

    private S3Client buildClient(StorageProperties properties) {
        String endpointUrl = requireConfigured(properties.endpointUrl(), "STORAGE_ENDPOINT_URL");
        String region = requireConfigured(properties.region(), "STORAGE_REGION");
        String accessKey = requireConfigured(properties.accessKeyId(), "STORAGE_ACCESS_KEY_ID");
        String secretKey = requireConfigured(properties.secretAccessKey(), "STORAGE_SECRET_ACCESS_KEY");

        return S3Client.builder()
                .endpointOverride(URI.create(endpointUrl))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .forcePathStyle(true)
                .build();
    }

    private String buildObjectKey(MultipartFile file) {
        String extension = resolveExtension(file.getOriginalFilename(), file.getContentType());
        return "products/" + UUID.randomUUID() + extension;
    }

    private String resolveExtension(String originalFilename, String contentType) {
        if (contentType != null) {
            String lower = contentType.toLowerCase(Locale.ROOT);
            if (lower.contains("jpeg") || lower.contains("jpg")) return ".jpg";
            if (lower.contains("png")) return ".png";
            if (lower.contains("webp")) return ".webp";
            if (lower.contains("gif")) return ".gif";
            if (lower.contains("svg")) return ".svg";
        }

        if (originalFilename != null && originalFilename.contains(".")) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            if (extension.length() <= 8) {
                return extension;
            }
        }

        return ".jpg";
    }

    private String buildPublicUrl(String objectKey) {
        String publicBaseUrl = storageProperties.publicBaseUrl();
        if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
            return joinUrl(normalizeBaseUrl(publicBaseUrl), objectKey);
        }

        return joinUrl(requireConfigured(storageProperties.endpointUrl(), "STORAGE_ENDPOINT_URL"),
                requireConfigured(storageProperties.bucketName(), "STORAGE_BUCKET_NAME"),
                objectKey);
    }

    private String normalizeBaseUrl(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        return "https://" + trimmed;
    }

    private String joinUrl(String... parts) {
        StringBuilder url = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part == null || part.isBlank()) continue;
            String normalized = part.trim();

            // Keep the scheme on the first URL segment (e.g. https://host)
            // while still trimming extra slashes around path segments.
            if (i == 0) {
                normalized = normalized.replaceAll("/+$", "");
            } else {
                normalized = normalized.replaceAll("^/+|/+$", "");
            }

            if (url.length() > 0 && url.charAt(url.length() - 1) != '/') {
                url.append('/');
            }
            url.append(normalized);
        }
        return url.toString();
    }

    private String requireConfigured(String value, String envName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(envName + " must be configured for image uploads.");
        }
        return value.trim();
    }

    public record UploadedFile(String url, String key) {
    }
}