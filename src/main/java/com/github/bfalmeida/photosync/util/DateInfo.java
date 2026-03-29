package com.github.bfalmeida.photosync.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateInfo {

    private static final Pattern IMG_PATTERN = Pattern.compile("IMG_(\\d{4})(\\d{2})(\\d{2})");
    private static final Pattern VID_PATTERN = Pattern.compile("VID_(\\d{4})(\\d{2})(\\d{2})");
    private static final Pattern YYYY_MM_DD_PATTERN = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})");

    private final int year;
    private final int month;

    private DateInfo(int year, int month) {
        this.year = year;
        this.month = month;
    }

    public static Optional<DateInfo> fromFileName(String fileName) {
        Matcher imgMatcher = IMG_PATTERN.matcher(fileName);
        if (imgMatcher.find()) {
            return Optional.of(new DateInfo(
                    Integer.parseInt(imgMatcher.group(1)),
                    Integer.parseInt(imgMatcher.group(2))
            ));
        }

        Matcher vidMatcher = VID_PATTERN.matcher(fileName);
        if (vidMatcher.find()) {
            return Optional.of(new DateInfo(
                    Integer.parseInt(vidMatcher.group(1)),
                    Integer.parseInt(vidMatcher.group(2))
            ));
        }

        Matcher dateMatcher = YYYY_MM_DD_PATTERN.matcher(fileName);
        if (dateMatcher.find()) {
            return Optional.of(new DateInfo(
                    Integer.parseInt(dateMatcher.group(1)),
                    Integer.parseInt(dateMatcher.group(2))
            ));
        }

        return Optional.empty();
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public YearMonth getYearMonth() {
        return YearMonth.of(year, month);
    }

    public String getDestinationPath(String fileName, String undatedFolder) {
        if (undatedFolder != null && !undatedFolder.isEmpty()) {
            return undatedFolder + "/" + fileName;
        }
        String folderName = determineFolderName(fileName);
        return String.format("%04d/%02d/%s/%s", year, month, folderName, fileName);
    }

    private String determineFolderName(String fileName) {
        if (fileName.toLowerCase().endsWith(".mp4") ||
            fileName.toLowerCase().endsWith(".mov") ||
            fileName.toLowerCase().endsWith(".avi") ||
            fileName.toLowerCase().endsWith(".mkv")) {
            return "Videos";
        }
        return "Photos";
    }
}
