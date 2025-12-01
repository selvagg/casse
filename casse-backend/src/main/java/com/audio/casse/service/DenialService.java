package com.audio.casse.service;

import com.audio.casse.models.Song;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DenialService {

    private final EmailService emailService;
    private final PendingApprovalService pendingApprovalService;
    private final CloudflareR2Service cloudflareR2Service;

    public void denySong(String uploaderEmail, String songTitle) {
        try {
            Song song = pendingApprovalService.getPendingApproval(uploaderEmail, songTitle);
            if (song != null) {
                // 1. Delete Redis entry
                pendingApprovalService.removePendingApproval(uploaderEmail, songTitle);
                log.info("Deleted Redis entry for song '{}' by '{}'", songTitle, uploaderEmail);

                // 2. Delete S3 object
                cloudflareR2Service.deleteFile(song.getStorageAccessKey(), uploaderEmail);
                log.info("Deleted S3 object for song '{}' by '{}' with key '{}'", songTitle, uploaderEmail, song.getStorageAccessKey());

                // 3. Send denial email
                emailService.sendSongDeniedEmail(uploaderEmail, songTitle);
                log.info("Sent denial email to '{}' for song '{}'", uploaderEmail, songTitle);
            } else {
                log.warn("Attempted to deny song '{}' by '{}' but no pending approval found.", songTitle, uploaderEmail);
            }
        } catch (JsonProcessingException e) {
            log.error("Error processing JSON for song denial: {}", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            log.error("An unexpected error occurred during song denial: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
