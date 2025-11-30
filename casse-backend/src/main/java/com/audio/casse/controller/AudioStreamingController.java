package com.audio.casse.controller;

import com.audio.casse.models.Song;
import com.audio.casse.service.CloudflareR2Service;
import com.audio.casse.service.PendingApprovalService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/audio")
@AllArgsConstructor
public class AudioStreamingController {

    private final CloudflareR2Service r2Service;
    private final PendingApprovalService approvalService;

    @PostMapping("/upload")
    public String handleUpload(@ModelAttribute Song song,
                               @RequestParam("file") MultipartFile file, // Add MultipartFile parameter
                               @AuthenticationPrincipal OAuth2User principal) throws JsonProcessingException, IOException { // Add IOException
        String email = principal.getAttribute("email");
        song.setEmail(email);

        if (!file.isEmpty()) {
            try {
                song.setStorageAccessKey(file.getOriginalFilename());
                // Upload file to R2
                r2Service.uploadFile(file, email);
            } catch (IOException e) {
                e.printStackTrace();
                return "redirect:/home?error=file_upload_failed";
            }
        } else {
            return "redirect:/home?error=no_file_selected";
        }

        approvalService.storePendingApproval(email, song);
        return "redirect:/home?success=song_submitted";
    }

    @GetMapping("/list")
    public ResponseEntity<List<String>> listAudioFiles(Authentication authentication) {
        List<String> fileNames = r2Service.listFiles(authentication.getName());
        return ResponseEntity.ok(fileNames);
    }

    @GetMapping("/stream-direct/{fileName}")
    public ResponseEntity<InputStreamResource> streamAudio(@PathVariable String fileName, Authentication authentication) {
        try {
            ResponseInputStream<GetObjectResponse> responseFromS3 = r2Service.streamFile(fileName, authentication.getName());
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
