package com.audio.casse.controller;

import com.audio.casse.models.Song;
import com.audio.casse.repository.SongsRepository;
import com.audio.casse.service.CloudflareR2Service;
import com.audio.casse.service.DenialService;
import com.audio.casse.service.EmailService;
import com.audio.casse.service.PendingApprovalService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

/**
 * REST controller for handling audio streaming and song management operations.
 * This includes uploading songs, listing user's songs, streaming audio and album art,
 * and approving/denying pending songs.
 */
@RestController
@RequestMapping("/audio")
@AllArgsConstructor
@Slf4j
@Tag(name = "Audio Streaming and Management", description = "Operations for uploading, streaming, and managing songs")
public class AudioStreamingController {

    private final CloudflareR2Service r2Service;
    private final PendingApprovalService approvalService;
    private final SongsRepository songsRepository;
    private final EmailService emailService;
    private final DenialService denialService;

    /**
     * Handles the upload of a new song and its album art.
     * The uploaded song is submitted for approval.
     *
     * @param song The song metadata.
     * @param audioFile The audio file to upload.
     * @param albumArtFile The album art image file to upload.
     * @param principal The authenticated OAuth2 user principal.
     * @return A redirect string indicating the success or failure of the upload.
     * @throws IOException If an error occurs during file processing.
     */
    @Operation(summary = "Upload a new song",
               description = "Uploads an audio file and its album art, then submits the song for approval.")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String handleUpload(
            @Parameter(description = "Song metadata including title, artists, album, etc.")
            @ModelAttribute Song song,
            @Parameter(description = "The audio file to upload.")
            @RequestPart("audioFile") MultipartFile audioFile,
            @Parameter(description = "The album art image file to upload.")
            @RequestPart("albumArtFile") MultipartFile albumArtFile,
            @AuthenticationPrincipal OAuth2User principal) throws IOException {
        String email = principal.getAttribute("email");
        song.setEmail(email);

        if (!audioFile.isEmpty()) {
            try {
                String originalFilename = audioFile.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String newFileName = song.getTitle() + extension;
                song.setStorageAccessKey(newFileName);
                log.info("Setting storageAccessKey in AudioStreamingController: {}", song.getStorageAccessKey());
                r2Service.uploadSong(audioFile, email, newFileName);
            } catch (IOException e) {
                log.error("File upload failed for user {}: {}", email, e.getMessage());
                return "redirect:/home?error=file_upload_failed";
            }
        } else {
            log.warn("No file selected for upload by user {}", email);
            return "redirect:/home?error=no_file_selected";
        }

        if (!albumArtFile.isEmpty()) {
            try {
                String originalFilename = albumArtFile.getOriginalFilename();
                r2Service.uploadAlbumArt(albumArtFile, email, originalFilename);
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

    /**
     * Retrieves a list of songs uploaded by the authenticated user.
     *
     * @param authentication The authentication object containing user details.
     * @return A ResponseEntity containing a list of songs.
     */
    @Operation(summary = "List user's uploaded songs",
               description = "Retrieves all songs uploaded by the currently authenticated user.")
    @GetMapping("/list")
    public ResponseEntity<List<Song>> listAudioFiles(
            @Parameter(hidden = true) Authentication authentication) {
        List<Song> songs = songsRepository.findByEmail(authentication.getName());
        return ResponseEntity.ok(songs);
    }

    /**
     * Streams the album art for a given file name.
     *
     * @param fileName The name of the album art file.
     * @param authentication The authentication object containing user details.
     * @return A ResponseEntity containing the album art as an InputStreamResource.
     */
    @Operation(summary = "Stream album art",
               description = "Streams the album art image associated with a song.")
    @GetMapping("/album-art/{fileName}")
    public ResponseEntity<InputStreamResource> streamAlbumArt(
            @Parameter(description = "The file name of the album art.")
            @PathVariable String fileName,
            @Parameter(hidden = true) Authentication authentication) {
        try {
            ResponseInputStream<GetObjectResponse> responseFromS3 = r2Service.streamAlbumArt(fileName, authentication.getName());
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

    /**
     * Streams an audio file directly from storage.
     *
     * @param fileName The name of the audio file.
     * @param authentication The authentication object containing user details.
     * @return A ResponseEntity containing the audio file as an InputStreamResource.
     */
    @Operation(summary = "Stream audio directly",
               description = "Streams an audio file directly from storage using its file name.")
    @GetMapping("/stream-direct/{fileName}")
    public ResponseEntity<InputStreamResource> streamAudio(
            @Parameter(description = "The file name of the audio to stream.")
            @PathVariable String fileName,
            @Parameter(hidden = true) Authentication authentication) {
        try {
            ResponseInputStream<GetObjectResponse> responseFromS3 = r2Service.streamSong(fileName, authentication.getName());
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

    /**
     * Streams an approved audio file. This endpoint is typically used for playing approved songs.
     *
     * @param uploaderEmail The email of the user who uploaded the song.
     * @param songTitle The title of the song.
     * @param storageAccessKey The storage access key for the audio file.
     * @return A ResponseEntity containing the audio file as an InputStreamResource.
     */
    @Operation(summary = "Stream approved audio",
               description = "Streams an audio file that has been approved, identified by uploader email, song title, and storage key.")
    @GetMapping("/stream")
    public ResponseEntity<InputStreamResource> streamApprovedAudio(
            @Parameter(description = "The email of the user who uploaded the song.")
            @RequestParam("email") String uploaderEmail,
            @Parameter(description = "The title of the song.")
            @RequestParam("title") String songTitle,
            @Parameter(description = "The storage access key for the audio file.")
            @RequestParam("key") String storageAccessKey) {
        try {
            log.info("Streaming approved audio for song '{}' by '{}' with key '{}'", songTitle, uploaderEmail, storageAccessKey);
            ResponseInputStream<GetObjectResponse> responseFromS3 = r2Service.streamSong(storageAccessKey, uploaderEmail);
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

    /**
     * Approves a pending song. Once approved, the song becomes available for streaming.
     *
     * @param uploaderEmail The email of the user who uploaded the song.
     * @param songTitle The title of the song to approve.
     * @return A ResponseEntity indicating the success or failure of the approval.
     */
    @Operation(summary = "Approve a pending song",
               description = "Approves a song that was previously submitted for approval, making it available for public access.")
    @GetMapping("/approve")
    public ResponseEntity<String> approveSong(
            @Parameter(description = "The email of the user who uploaded the song.")
            @RequestParam("email") String uploaderEmail,
            @Parameter(description = "The title of the song to approve.")
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

    /**
     * Denies a pending song. The song will not be made available for streaming.
     *
     * @param uploaderEmail The email of the user who uploaded the song.
     * @param songTitle The title of the song to deny.
     * @return A ResponseEntity indicating the success or failure of the denial.
     */
    @Operation(summary = "Deny a pending song",
               description = "Denies a song that was submitted for approval, preventing it from being publicly accessible.")
    @GetMapping("/deny")
    public ResponseEntity<String> denySong(
            @Parameter(description = "The email of the user who uploaded the song.")
            @RequestParam("email") String uploaderEmail,
            @Parameter(description = "The title of the song to deny.")
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
