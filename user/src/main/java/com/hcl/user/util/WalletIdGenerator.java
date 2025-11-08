package com.hcl.user.util;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class WalletIdGenerator {
    
    public String generateWalletId() {
        // Combine timestamp and UUID for a unique wallet ID
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return "W" + timestamp.substring(timestamp.length() - 6) + uuid.toUpperCase();
    }
}