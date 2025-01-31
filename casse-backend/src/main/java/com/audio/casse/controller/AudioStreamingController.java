package com.audio.casse.controller;

import com.audio.casse.service.CloudflareR2Service;
import java.io.IOException;
import java.util.List;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@RestController
@RequestMapping("/audio")
public class AudioStreamingController {

    private final CloudflareR2Service r2Service;

    public AudioStreamingController(CloudflareR2Service r2Service) {
        this.r2Service = r2Service;
    }

    @PostMapping("upload")
    public ResponseEntity<String> uploadAudio(@RequestParam("file") MultipartFile file) {
        try {
            r2Service.uploadFile(file);
            return ResponseEntity.ok("File uploaded successfully!");
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<String>> listAudioFiles() {
        List<String> fileNames = r2Service.listFiles();
        return ResponseEntity.ok(fileNames);
    }

    @GetMapping("/stream-direct/{fileName}")
    public ResponseEntity<InputStreamResource> streamAudio(@PathVariable String fileName) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                .bucket(r2Service.getBucketName())
                .key(fileName)
                .build();

            ResponseInputStream<GetObjectResponse> responseFromS3 = r2Service.streamFile(fileName);
            GetObjectResponse objectResponse = responseFromS3.response();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(objectResponse.contentType()));
            headers.setContentLength(objectResponse.contentLength());
            headers.set("Accept-Ranges", "bytes");

            return new ResponseEntity<>(new InputStreamResource(responseFromS3), headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

