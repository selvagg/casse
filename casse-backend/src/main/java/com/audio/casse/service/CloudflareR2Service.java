package com.audio.casse.service;

import static software.amazon.awssdk.core.sync.RequestBody.fromInputStream;

import java.io.IOException;
import java.util.List;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;

@Service
@Getter
public class CloudflareR2Service {

    private final S3Client s3Client;
    private final String bucketName;
    private final UserService userService;
    private final String endpoint;

    public CloudflareR2Service(
            @Value("${cloudflare.r2.access-key}") String accessKey,
            @Value("${cloudflare.r2.secret-key}") String secretKey,
            @Value("${cloudflare.r2.endpoint}") String endpoint,
            @Value("${cloudflare.r2.bucket}") String bucketName, // Match the YAML key
            UserService userService) {

        this.bucketName = bucketName;
        this.userService = userService;
        this.endpoint = endpoint;

        this.s3Client = S3Client.builder()
                .endpointOverride(java.net.URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.US_EAST_1) // MinIO and R2 both work with this
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build()) // Required for MinIO
                .build();
    }

    public void uploadFile(MultipartFile file) throws IOException {
        String objectKey = userService.getId() + "/" + file.getOriginalFilename();

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentLength(file.getSize())
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request, fromInputStream(file.getInputStream(), file.getSize()));
    }

    public String generatePresignedUrl(String fileName) {
        String objectKey = userService.getId() + "/" + fileName;
        return endpoint.contains("localhost") ?
                String.format("%s/%s/%s", endpoint, bucketName, objectKey) : // MinIO Direct URL
                String.format("https://%s.r2.cloudflarestorage.com/%s/%s", bucketName, bucketName, objectKey); // Cloudflare R2
    }

    public List<String> listFiles() {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(userService.getId() + "/")
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);
        return response.contents().stream()
                .map(S3Object::key)
                .map(key -> key.split("/")[1])
                .toList();
    }

    public ResponseInputStream<GetObjectResponse> streamFile(String fileName) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(userService.getId() + "/" + fileName)
                .build();

        return s3Client.getObject(request);
    }
}
