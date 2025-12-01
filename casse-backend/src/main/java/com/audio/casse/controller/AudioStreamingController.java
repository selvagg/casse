package com.audio.casse.controller;

import com.audio.casse.models.Song;
import com.audio.casse.repository.SongsRepository;
import com.audio.casse.service.CloudflareR2Service;
import com.audio.casse.service.DenialService;
import com.audio.casse.service.EmailService;
import com.audio.casse.service.PendingApprovalService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AudioStreamingController {

    private final CloudflareR2Service r2Service;
    private final PendingApprovalService approvalService;
    private final SongsRepository songsRepository;
    private final EmailService emailService;
    private final DenialService denialService; // Inject DenialService

    @PostMapping("/upload")
    public String handleUpload(@ModelAttribute Song song,
                               @RequestParam("file") MultipartFile file,
                               @RequestParam("albumArt") MultipartFile albumArt,
                               @AuthenticationPrincipal OAuth2User principal) throws IOException {
        String email = principal.getAttribute("email");
        song.setEmail(email);

        if (!file.isEmpty()) {
            try {
                String originalFilename = file.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String newFileName = song.getTitle() + extension;
                song.setStorageAccessKey(newFileName);
                log.info("Setting storageAccessKey in AudioStreamingController: {}", song.getStorageAccessKey());
                r2Service.uploadFile(file, email, newFileName);
            } catch (IOException e) {
                log.error("File upload failed for user {}: {}", email, e.getMessage());
                return "redirect:/home?error=file_upload_failed";
            }
        } else {
            log.warn("No file selected for upload by user {}", email);
            return "redirect:/home?error=no_file_selected";
        }

        if (!albumArt.isEmpty()) {
            try {
                String originalFilename = albumArt.getOriginalFilename();
                r2Service.uploadFile(albumArt, email, originalFilename);
                song.setAlbumArt(originalFilename);
            } catch (IOException e) {
                log.error("Album art upload failed for user {}: {}", email, e.getMessage());
                return "redirect:/home?error=album_art_upload_failed";
            }
        }

        approvalService.storePendingApproval(email, song);
        log.info("Song '{}' submitted for approval by '{}'. StorageAccessKey: {}", song.getTitle(), email, song.getStorageAccessKey());
        return "redirect:/home?success=song_submitted";
    }

    @GetMapping("/list")
    public ResponseEntity<List<Song>> listAudioFiles(Authentication authentication) {
        List<Song> songs = songsRepository.findByEmail(authentication.getName());
        return ResponseEntity.ok(songs);
    }

    @GetMapping("/album-art/{fileName}")
    public ResponseEntity<InputStreamResource> streamAlbumArt(@PathVariable String fileName, Authentication authentication) {
        try {
            ResponseInputStream<GetObjectResponse> responseFromS3 = r2Service.streamFile(fileName, authentication.getName());
            GetObjectResponse objectResponse = responseFromS3.response();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(objectResponse.contentType()));
            headers.setContentLength(objectResponse.contentLength());

            return new ResponseEntity<>(new InputStreamResource(responseFromS3), headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error streaming album art '{}' for user {}: {}", fileName, authentication.getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
            log.error("Error streaming audio direct '{}' for user {}: {}", fileName, authentication.getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/stream")
    public ResponseEntity<InputStreamResource> streamApprovedAudio(
            @RequestParam("email") String uploaderEmail,
            @RequestParam("title") String songTitle,
            @RequestParam("key") String storageAccessKey) {
        try {
            log.info("Streaming approved audio for song '{}' by '{}' with key '{}'", songTitle, uploaderEmail, storageAccessKey);
            ResponseInputStream<GetObjectResponse> responseFromS3 = r2Service.streamFile(storageAccessKey, uploaderEmail);
            GetObjectResponse objectResponse = responseFromS3.response();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(objectResponse.contentType()));
            headers.setContentLength(objectResponse.contentLength());
            headers.set("Accept-Ranges", "bytes");
            headers.setContentDispositionFormData("attachment", songTitle + ".mp3");

            return new ResponseEntity<>(new InputStreamResource(responseFromS3), headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error streaming approved audio for song '{}' by '{}' with key '{}': {}", songTitle, uploaderEmail, storageAccessKey, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/approve")
    public ResponseEntity<String> approveSong(
            @RequestParam("email") String uploaderEmail,
            @RequestParam("title") String songTitle) {
        try {
            log.info("Attempting to approve song '{}' by '{}'", songTitle, uploaderEmail);
            Song songToApprove = approvalService.getPendingApproval(uploaderEmail, songTitle);

            if (songToApprove != null) {
                songsRepository.save(songToApprove);
                approvalService.removePendingApproval(uploaderEmail, songTitle);
                emailService.sendSongApprovedEmail(uploaderEmail, songTitle);
                log.info("Song '{}' by '{}' approved successfully. StorageAccessKey: {}", songTitle, uploaderEmail, songToApprove.getStorageAccessKey());
                return ResponseEntity.ok("Song '" + songTitle + "' by '" + uploaderEmail + "' approved successfully.");
            } else {
                log.warn("Song '{}' by '{}' not found in pending approvals for approval.", songTitle, uploaderEmail);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Song not found in pending approvals.");
            }
        } catch (JsonProcessingException e) {
            log.error("Error processing song data for approval of '{}' by '{}': {}", songTitle, uploaderEmail, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing song data.");
        } catch (Exception e) {
            log.error("An unexpected error occurred during approval of '{}' by '{}': {}", songTitle, uploaderEmail, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred during approval.");
        }
    }

    @GetMapping("/deny")
    public ResponseEntity<String> denySong(
            @RequestParam("email") String uploaderEmail,
            @RequestParam("title") String songTitle) {
        try {
            log.info("Attempting to deny song '{}' by '{}'", songTitle, uploaderEmail);
            denialService.denySong(uploaderEmail, songTitle);
            return ResponseEntity.ok("Song '" + songTitle + "' by '" + uploaderEmail + "' denied successfully.");
        } catch (Exception e) {
            log.error("An unexpected error occurred during denial of '{}' by '{}': {}", songTitle, uploaderEmail, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred during denial.");
        }
    }
}
