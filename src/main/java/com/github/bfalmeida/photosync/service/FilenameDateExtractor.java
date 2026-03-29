package com.github.bfalmeida.photosync.service;

import java.time.YearMonth;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilenameDateExtractor {

    private static final Pattern YYYY_MM_DD_HH_MM_SS_PATTERN = 
        Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})_(\\d{2})-(\\d{2})-(\\d{2})");
    private static final Pattern IMG_YYYYMMDD_WA_PATTERN = 
        Pattern.compile("IMG-(\\d{4})(\\d{2})(\\d{2})-WA(\\d{4})");
    private static final Pattern IMG_YYYYMMDD_HHMMSS_PATTERN = 
        Pattern.compile("IMG_(\\d{4})(\\d{2})(\\d{2})_(\\d{2})(\\d{2})(\\d{2})");
    private static final Pattern VID_YYYYMMDD_HHMMSS_PATTERN = 
        Pattern.compile("VID_(\\d{4})(\\d{2})(\\d{2})_(\\d{2})(\\d{2})(\\d{2})");

    public Optional<DateInfo> extract(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return Optional.empty();
        }

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

    public static class DateInfo {
        private final int year;
        private final int month;
        private final boolean whatsApp;

        public DateInfo(int year, int month, boolean whatsApp) {
            this.year = year;
            this.month = month;
            this.whatsApp = whatsApp;
        }

        public int getYear() {
            return year;
        }

        public int getMonth() {
            return month;
        }

        public boolean isWhatsApp() {
            return whatsApp;
        }

        public YearMonth getYearMonth() {
            return YearMonth.of(year, month);
        }
    }
}
