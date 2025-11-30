package com.audio.casse.service;

import com.audio.casse.config.properties.PendingApprovalProperties;
import com.audio.casse.models.Song;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PendingApprovalService {

    private static final String PENDING_APPROVAL_PREFIX = "pending-approval:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final PendingApprovalProperties pendingApprovalProperties;

    public void storePendingApproval(String principalEmail, Song song) throws JsonProcessingException {
        String key = PENDING_APPROVAL_PREFIX + principalEmail + ":" + song.getTitle();
        String value = objectMapper.writeValueAsString(song);
        redisTemplate.opsForValue().set(key, value, pendingApprovalProperties.getTtl());
    }
}
