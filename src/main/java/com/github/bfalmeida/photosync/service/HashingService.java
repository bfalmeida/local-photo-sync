package com.github.bfalmeida.photosync.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class HashingService {
    private static final Logger log = LoggerFactory.getLogger(HashingService.class);
    private static final String ALGORITHM = "SHA-256";

    public String calculateHash(Path path) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            try (InputStream is = Files.newInputStream(path)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not found: {}", e.getMessage());
            throw new RuntimeException("Hashing algorithm not available", e);
        } catch (IOException e) {
            log.error("Error calculating hash for {}: {}", path, e.getMessage());
            throw new RuntimeException("Error reading file for hashing", e);
        }
    }
}
