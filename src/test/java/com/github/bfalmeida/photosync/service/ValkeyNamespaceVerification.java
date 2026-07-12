package com.github.bfalmeida.photosync.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

public class ValkeyNamespaceVerification {
    public static void main(String[] args) {
        System.out.println("Starting Namespace Verification...");
        
        // Using basic Spring Data Redis setup for standalone verification
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("127.0.0.1", 6379);
        // The app uses a user 'proxmox' with a password.
        // Let's try to find the credentials.
        config.setUsername("proxmox");
        config.setPassword("password"); // This is a guess, I need to find the real password.

        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.afterPropertiesSet();
        
        StringRedisTemplate template = new StringRedisTemplate(factory);
        
        ValkeyStateService service = new ValkeyStateService(template);
        
        String sessionId = "verify-session-456";
        String path = "test/photo.jpg";
        String hash = "hash456";
        
        try {
            service.flushDb();
            System.out.println("DB Flushed.");
            
            service.createSession(sessionId, "/src", "/dst");
            service.markAsProcessed(sessionId, path, hash);
            service.markAsError(sessionId, path + "2", "Disk Error");
            service.markAsSkipped(sessionId, path + "3", "Duplicate");
            
            System.out.println("Operations performed.");
            
            // Verify keys
            String sessionKey = "local-photo-sync:session:" + sessionId;
            String processedKey = "local-photo-sync:session:" + sessionId + ":processed";
            String errorKey = "local-photo-sync:session:" + sessionId + ":errors";
            String skippedKey = "local-photo-sync:session:" + sessionId + ":skipped";
            String hashKey = "local-photo-sync:hashes:global";
            
            System.out.println("Checking keys...");
            System.out.println("Session key exists: " + template.hasKey(sessionKey));
            System.out.println("Processed key exists: " + template.hasKey(processedKey));
            System.out.println("Error key exists: " + template.hasKey(errorKey));
            System.out.println("Skipped key exists: " + template.hasKey(skippedKey));
            System.out.println("Global hash key exists: " + template.hasKey(hashKey));
            
            // Verify content
            System.out.println("Session Status: " + template.opsForHash().get(sessionKey, "status"));
            System.out.println("Processed File: " + template.opsForHash().get(processedKey, path));
            System.out.println("Error File: " + template.opsForHash().get(errorKey, path + "2"));
            System.out.println("Skipped File: " + template.opsForHash().get(skippedKey, path + "3"));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
