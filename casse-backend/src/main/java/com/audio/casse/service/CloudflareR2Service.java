package com.audio.casse.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.List;

import static software.amazon.awssdk.core.sync.RequestBody.fromInputStream;


@Service
@Getter
public class CloudflareR2Service {

    private final S3Client s3Client;
    private final String bucketName;
    private final String endpoint;
    private final Environment environment;

    public CloudflareR2Service(
            @Value("${cloudflare.r2.access-key}") String accessKey,
            @Value("${cloudflare.r2.secret-key}") String secretKey,
            @Value("${cloudflare.r2.endpoint}") String endpoint,
            @Value("${cloudflare.r2.bucket-name}") String bucketName,
            Environment environment) {

        this.bucketName = bucketName;
        this.endpoint = endpoint;
        this.environment = environment;

        this.s3Client = S3Client.builder()
                .endpointOverride(java.net.URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.US_EAST_1) // MinIO and R2 both work with this
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build()) // Required for MinIO
                .build();
    }

    public void uploadFile(MultipartFile file, String userName, String fileName) throws IOException {
        String objectKey = userName + "/" + fileName;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentLength(file.getSize())
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request, fromInputStream(file.getInputStream(), file.getSize()));
    }

    public List<String> listFiles(String userName) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(userName + "/")
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);
        return response.contents().stream()
                .map(S3Object::key)
                .map(key -> key.substring(key.indexOf("/") + 1))
                .toList();
    }

    public ResponseInputStream<GetObjectResponse> streamFile(String fileName, String userName) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(userName + "/" + fileName)
                .build();

        return s3Client.getObject(request);
    }

    public void deleteFile(String fileName, String userName) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(userName + "/" + fileName)
                .build();

        s3Client.deleteObject(request);
    }
}
