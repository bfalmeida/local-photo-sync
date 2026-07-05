package com.github.bfalmeida.photosync.service;

import com.github.bfalmeida.photosync.model.HealthStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class HealthMonitorService {
    private static final Logger log = LoggerFactory.getLogger(HealthMonitorService.class);
    
    private final SyncStateRepository stateRepository;

    public HealthMonitorService(SyncStateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }

    /**
     * Checks the connectivity to the Valkey instance safely.
     */
    public HealthStatus checkValkey() {
        long start = System.currentTimeMillis();
        try {
            if (stateRepository.ping()) {
                long latency = System.currentTimeMillis() - start;
                return new HealthStatus(true, "Valkey Connected", latency);
            } else {
                return new HealthStatus(false, "Valkey responded with failure", -1);
            }
        } catch (Exception e) {
            log.error("Valkey health check failed: {}", e.getMessage());
            return new HealthStatus(false, "Valkey Offline: " + e.getMessage(), -1);
        }
    }

    /**
     * Checks the available space on the destination drive.
     */
    public HealthStatus checkDiskSpace(Path destinationPath) {
        try {
            if (destinationPath == null || !Files.exists(destinationPath)) {
                return new HealthStatus(false, "Disk Path Invalid", -1);
            }
            
            FileStore store = Files.getFileStore(destinationPath);
            long usableSpace = store.getUsableSpace();
            long totalSpace = store.getTotalSpace();
            double usagePercent = (double)(totalSpace - usableSpace) / totalSpace * 100;
            
            if (usagePercent > 95) {
                return new HealthStatus(false, String.format("Disk Critical: %.1f%% full", usagePercent), -1);
            } else if (usagePercent > 80) {
                return new HealthStatus(true, String.format("Disk Warning: %.1f%% full", usagePercent), -1);
            }
            
            return new HealthStatus(true, "Disk Healthy", -1);
        } catch (IOException e) {
            log.error("Disk health check failed: {}", e.getMessage());
            return new HealthStatus(false, "Disk Error: " + e.getMessage(), -1);
        }
    }
}
