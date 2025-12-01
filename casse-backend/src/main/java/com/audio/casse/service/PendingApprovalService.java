package com.audio.casse.service;

import com.audio.casse.config.properties.PendingApprovalProperties;
import com.audio.casse.models.Song;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class PendingApprovalService {

    private static final String PENDING_APPROVAL_PREFIX = "pending-approval:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final PendingApprovalProperties pendingApprovalProperties;
    private final EmailService emailService;

    public void storePendingApproval(String principalEmail, Song song) throws JsonProcessingException {
        String key = PENDING_APPROVAL_PREFIX + principalEmail + ":" + song.getTitle();
        String value = objectMapper.writeValueAsString(song);
        redisTemplate.opsForValue().set(key, value, pendingApprovalProperties.getTtl());
        log.info("Storing pending approval for song '{}' by '{}'. StorageAccessKey: {}", song.getTitle(), principalEmail, song.getStorageAccessKey());

        // Send approval email
        emailService.sendApprovalEmail(principalEmail, song.getTitle(), song.getStorageAccessKey());
    }

    public Song getPendingApproval(String uploaderEmail, String songTitle) throws JsonProcessingException {
        String key = PENDING_APPROVAL_PREFIX + uploaderEmail + ":" + songTitle;
        String songJson = redisTemplate.opsForValue().get(key);
        if (songJson != null) {
            Song song = objectMapper.readValue(songJson, Song.class);
            log.debug("Retrieved pending approval for song '{}' by '{}'. StorageAccessKey: {}", songTitle, uploaderEmail, song.getStorageAccessKey());
            return song;
        }
        log.debug("No pending approval found for song '{}' by '{}'", songTitle, uploaderEmail);
        return null;
    }

    public void removePendingApproval(String principalEmail, String songTitle) {
        String key = PENDING_APPROVAL_PREFIX + principalEmail + ":" + songTitle;
        redisTemplate.delete(key);
        log.info("Removed pending approval for song '{}' by '{}'", songTitle, principalEmail);
    }
}
