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
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
@Getter
public class CloudflareR2Service {

    private final S3Client s3Client;
    private final String bucketName;
    private final UserService userService;

    public CloudflareR2Service(
        @Value("${cloudflare.r2.access-key}") String accessKey,
        @Value("${cloudflare.r2.secret-key}") String secretKey,
        @Value("${cloudflare.r2.endpoint}") String endpoint,
        @Value("${cloudflare.r2.bucket-name}") String bucketName, UserService userService) {

        this.bucketName = bucketName;
        this.userService = userService;

        this.s3Client = S3Client.builder()
            .endpointOverride(java.net.URI.create(endpoint))
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
            .region(Region.US_EAST_1) // R2 uses "auto" but needs a placeholder
            .build();
    }

    public void uploadFile(MultipartFile file) throws IOException {
        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(userService.getId() + "/" + file.getOriginalFilename())
            .contentLength(file.getSize())
            .contentType(file.getContentType())
            .build();

        s3Client.putObject(request, fromInputStream(file.getInputStream(), file.getSize()));
    }

    public String generatePresignedUrl(String fileName) {
        return String.format("https://%s.r2.cloudflarestorage.com/%s/%s", bucketName, bucketName, fileName);
    }

    public List<String> listFiles() {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
            .bucket(bucketName)
            .prefix(userService.getId() + "/")
            .delimiter("/")// List all files under the bucket
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
            .key(userService.getId() + "/" + fileName) // Path to your file
            .build();

        return s3Client.getObject(request);
    }
}

