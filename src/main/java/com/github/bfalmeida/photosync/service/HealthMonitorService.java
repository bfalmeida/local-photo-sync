package com.github.bfalmeida.photosync.service;

import com.github.bfalmeida.photosync.model.HealthStatus;
import com.github.bfalmeida.photosync.ui.SyncEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class HealthMonitorService {
    private static final Logger log = LoggerFactory.getLogger(HealthMonitorService.class);
    
    private final SyncStateRepository stateRepository;
    private final SyncEventBus eventBus;

    public HealthMonitorService(SyncStateRepository stateRepository, SyncEventBus eventBus) {
        this.stateRepository = stateRepository;
        this.eventBus = eventBus;
    }

    /**
     * Scheduled polling engine. Runs every 30 seconds to update system health.
     */
    @Scheduled(fixedRate = 30000)
    public void pollHealth() {
        log.debug("Executing scheduled health check...");
        
        // Check Valkey
        HealthStatus valkeyStatus = checkValkey();
        eventBus.publish(SyncEventBus.EventType.LOG, valkeyStatus,
            valkeyStatus.healthy() ? "Health: Valkey OK" : "Health: Valkey ERROR - " + valkeyStatus.message());

        // Check Disk (using a generic root path as a proxy for system health, 
        // since we don't have the specific destination path yet in the service layer)
        HealthStatus diskStatus = checkDiskSpace(Path.of("/"));
        eventBus.publish(SyncEventBus.EventType.LOG, diskStatus,
            diskStatus.healthy() ? "Health: Disk OK" : "Health: Disk Warning - " + diskStatus.message());
    }

    /**
     * Checks the connectivity to the Valkey instance safely.
     */
    public HealthStatus checkValkey() {
        long start = System.currentTimeMillis();
        try {
            ValkeyResult<Boolean> pingResult = stateRepository.ping();
            
            if (pingResult.isSuccess() && Boolean.TRUE.equals(pingResult.getValue())) {
                long latency = System.currentTimeMillis() - start;
                return new HealthStatus(true, "Valkey Connected", latency);
            } else {
                String message = pingResult.isFailure() 
                    ? "Valkey " + pingResult.getError().getCode() + ": " + pingResult.getError().getMessage()
                    : "Valkey responded with failure";
                log.error("Valkey health check failed: {}", message);
                return new HealthStatus(false, message, -1);
            }
        } catch (Exception e) {
            log.error("Valkey health check failed: {}", e.getMessage());
            return new HealthStatus(false, "Valkey Offline: " + e.getMessage(), -1);
        }
    }

    /**
     * Checks the available space on a specific path.
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