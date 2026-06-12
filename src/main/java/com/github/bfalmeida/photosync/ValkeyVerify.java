package com.github.bfalmeida.photosync;

import com.github.bfalmeida.photosync.service.*;
import com.github.bfalmeida.photosync.model.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class ValkeyVerify {
    public static void main(String[] args) throws Exception {
        System.out.println("=== VALKEY RESUME VERIFICATION ===");
        
        ValkeyStateService stateService = new ValkeyStateService("127.0.0.1", 6379);
        MediaFileScanner scanner = new MediaFileScanner();
        FilenameDateExtractor extractor = new FilenameDateExtractor();
        ExifMetadataService exif = new ExifMetadataService();
        FileCopyService copy = new FileCopyService();
        
        SyncService syncService = new SyncService(scanner, extractor, exif, copy, stateService);
        
        Path source = Paths.get("/root/local-photo-sync/test-dataset/source");
        Path dest = Paths.get("/root/local-photo-sync/test-dataset/destination");
        
        System.out.println("Run 1: Initial Sync (Dry Run)");
        SyncStatistics s1 = syncService.synchronize(source, dest, false, "undated", false, false);
        System.out.println("Found: " + s1.getFilesFound() + ", Copied: " + s1.getFilesCopied());
        
        System.out.println("\nRun 2: Resume Sync (Dry Run)");
        SyncStatistics s2 = syncService.synchronize(source, dest, false, "undated", false, false);
        System.out.println("Found: " + s2.getFilesFound() + ", Skipped: " + s2.getFilesSkipped());
        
        System.out.println("\nRun 3: Reset Sync (Dry Run)");
        SyncStatistics s3 = syncService.synchronize(source, dest, false, "undated", false, true);
        System.out.println("Found: " + s3.getFilesFound() + ", Copied: " + s3.getFilesCopied());
    }
}
