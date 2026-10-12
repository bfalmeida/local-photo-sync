package com.github.bfalmeida.photosync.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import dev.brachtendorf.jimagehash.hashAlgorithms.DifferenceHash;
import dev.brachtendorf.jimagehash.hashAlgorithms.PerceptiveHash;

@Service
public class PerceptualHashService {
    private static final Logger log = LoggerFactory.getLogger(PerceptualHashService.class);
    private static final int DEFAULT_THRESHOLD = 10;

    public PerceptualHashService() {}

    public Optional<Long> computeHash(Path path) {
        try {
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(path.toFile());
            if (img == null) return Optional.empty();
            long hash = java.util.Arrays.hashCode(img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth()));
            return Optional.of(hash);
        } catch (Exception e) {
            log.debug("Error computing pHash for {}: {}", path, e.getMessage());
            return Optional.empty();
        }
    }

    public int hammingDistance(long hash1, long hash2) {
        return Long.bitCount(hash1 ^ hash2);
    }

    public boolean areSimilar(long hash1, long hash2, int threshold) {
        return hammingDistance(hash1, hash2) <= threshold;
    }

    public List<DuplicatePair> findDuplicates(List<Path> files, int threshold) {
        Map<Long, List<Path>> groups = new HashMap<>();
        for (Path file : files) {
            Optional<Long> hashOpt = computeHash(file);
            if (hashOpt.isEmpty()) continue;
            long hash = hashOpt.get();
            boolean matched = false;
            for (Map.Entry<Long, List<Path>> entry : groups.entrySet()) {
                if (areSimilar(entry.getKey(), hash, threshold)) {
                    entry.getValue().add(file);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                groups.put(hash, new ArrayList<>(List.of(file)));
            }
        }
        List<DuplicatePair> duplicates = new ArrayList<>();
        for (List<Path> group : groups.values()) {
            if (group.size() < 2) continue;
            for (int i = 0; i < group.size(); i++) {
                for (int j = i + 1; j < group.size(); j++) {
                    Path f1 = group.get(i);
                    Path f2 = group.get(j);
                    Optional<Long> h1 = computeHash(f1);
                    Optional<Long> h2 = computeHash(f2);
                    if (h1.isPresent() && h2.isPresent()) {
                        duplicates.add(new DuplicatePair(f1, f2, hammingDistance(h1.get(), h2.get())));
                    }
                }
            }
        }
        return duplicates;
    }

    public record DuplicatePair(Path file1, Path file2, int hammingDistance) {}
}
