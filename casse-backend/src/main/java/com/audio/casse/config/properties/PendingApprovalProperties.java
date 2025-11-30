package com.audio.casse.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "casse.pending.approval")
public class PendingApprovalProperties {

    private Duration ttl = Duration.ofDays(30);

}
