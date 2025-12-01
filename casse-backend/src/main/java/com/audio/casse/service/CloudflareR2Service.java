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
import java.util.stream.Collectors;

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

    private void uploadFile(MultipartFile file, String userName, String fileName, FileType fileType) throws IOException {
        String objectKey = userName + "/" + fileType.getFolder() + "/" + fileName;
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentLength(file.getSize())
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request, fromInputStream(file.getInputStream(), file.getSize()));
    }

    public void uploadSong(MultipartFile file, String userName, String fileName) throws IOException {
        uploadFile(file, userName, fileName, FileType.SONG);
    }

    public void uploadAlbumArt(MultipartFile file, String userName, String fileName) throws IOException {
        uploadFile(file, userName, fileName, FileType.ALBUM_ART);
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
                .collect(Collectors.toList());
    }

    private ResponseInputStream<GetObjectResponse> streamFile(String userName, String fileName, FileType fileType) {
        String objectKey = userName + "/" + fileType.getFolder() + "/" + fileName;
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        return s3Client.getObject(request);
    }

    public ResponseInputStream<GetObjectResponse> streamSong(String fileName, String userName) {
        return streamFile(userName, fileName, FileType.SONG);
    }

    public ResponseInputStream<GetObjectResponse> streamAlbumArt(String fileName, String userName) {
        return streamFile(userName, fileName, FileType.ALBUM_ART);
    }

    private void deleteFile(String userName, String fileName, FileType fileType) {
        String objectKey = userName + "/" + fileType.getFolder() + "/" + fileName;
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        s3Client.deleteObject(request);
    }

    public void deleteSong(String fileName, String userName) {
        deleteFile(userName, fileName, FileType.SONG);
    }

    public void deleteAlbumArt(String fileName, String userName) {
        deleteFile(userName, fileName, FileType.ALBUM_ART);
    }

    private enum FileType {
        SONG("songs"),
        ALBUM_ART("album-art");

        private final String folder;

        FileType(String folder) {
            this.folder = folder;
        }

        public String getFolder() {
            return folder;
        }
    }
}
