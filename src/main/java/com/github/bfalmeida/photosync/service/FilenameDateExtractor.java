package com.github.bfalmeida.photosync.service;

import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FilenameDateExtractor {

    private static final Pattern YYYY_MM_DD_HH_MM_SS_PATTERN = 
        Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})_(\\d{2})-(\\d{2})-(\\d{2})");
    private static final Pattern IMG_YYYYMMDD_WA_PATTERN = 
        Pattern.compile("IMG-(\\d{4})(\\d{2})(\\d{2})-WA(\\d{4})");
    private static final Pattern IMG_YYYYMMDD_HHMMSS_PATTERN = 
        Pattern.compile("IMG_(\\d{4})(\\d{2})(\\d{2})_(\\d{2})(\\d{2})(\\d{2})");
    private static final Pattern VID_YYYYMMDD_HHMMSS_PATTERN = 
        Pattern.compile("VID_(\\d{4})(\\d{2})(\\d{2})_(\\d{2})(\\d{2})(\\d{2})");

    private final Map<String, Optional<DateInfo>> cache = java.util.Collections.synchronizedMap(
        new java.util.LinkedHashMap<String, Optional<DateInfo>>(100, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Optional<DateInfo>> eldest) {
                return size() > 1000;
            }
        }
    );

    public Optional<DateInfo> extract(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return Optional.empty();
        }

        return cache.computeIfAbsent(fileName, this::doExtract);
    }

    private Optional<DateInfo> doExtract(String fileName) {
        Matcher yyyyMmDdMatcher = YYYY_MM_DD_HH_MM_SS_PATTERN.matcher(fileName);
        if (yyyyMmDdMatcher.find()) {
            return Optional.of(new DateInfo(
                    Integer.parseInt(yyyyMmDdMatcher.group(1)),
                    Integer.parseInt(yyyyMmDdMatcher.group(2)),
                    false
            ));
        }

        Matcher imgWaMatcher = IMG_YYYYMMDD_WA_PATTERN.matcher(fileName);
        if (imgWaMatcher.find()) {
            return Optional.of(new DateInfo(
                    Integer.parseInt(imgWaMatcher.group(1)),
                    Integer.parseInt(imgWaMatcher.group(2)),
                    true
            ));
        }

        Matcher imgMatcher = IMG_YYYYMMDD_HHMMSS_PATTERN.matcher(fileName);
        if (imgMatcher.find()) {
            return Optional.of(new DateInfo(
                    Integer.parseInt(imgMatcher.group(1)),
                    Integer.parseInt(imgMatcher.group(2)),
                    false
            ));
        }

        Matcher vidMatcher = VID_YYYYMMDD_HHMMSS_PATTERN.matcher(fileName);
        if (vidMatcher.find()) {
            return Optional.of(new DateInfo(
                    Integer.parseInt(vidMatcher.group(1)),
                    Integer.parseInt(vidMatcher.group(2)),
                    false
            ));
        }

        return Optional.empty();
    }

    public void clearCache() {
        cache.clear();
    }

    public record DateInfo(int year, int month, boolean whatsApp) {
        public YearMonth yearMonth() {
            return YearMonth.of(year, month);
        }
    }
}
