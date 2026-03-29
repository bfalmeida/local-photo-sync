package com.github.bfalmeida.photosync.util;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DateInfoTest {

    @Test
    void testParseImgPattern() {
        Optional<DateInfo> dateInfo = DateInfo.fromFileName("IMG_20240115.jpg");
        
        assertThat(dateInfo).isPresent();
        assertThat(dateInfo.get().getYear()).isEqualTo(2024);
        assertThat(dateInfo.get().getMonth()).isEqualTo(1);
        assertThat(dateInfo.get().getYearMonth()).isEqualTo(YearMonth.of(2024, 1));
    }

    @Test
    void testParseVidPattern() {
        Optional<DateInfo> dateInfo = DateInfo.fromFileName("VID_20231225.mp4");
        
        assertThat(dateInfo).isPresent();
        assertThat(dateInfo.get().getYear()).isEqualTo(2023);
        assertThat(dateInfo.get().getMonth()).isEqualTo(12);
    }

    @Test
    void testParseYYYYMMDDPattern() {
        Optional<DateInfo> dateInfo = DateInfo.fromFileName("2024-01-01-photo.jpg");
        
        assertThat(dateInfo).isPresent();
        assertThat(dateInfo.get().getYear()).isEqualTo(2024);
        assertThat(dateInfo.get().getMonth()).isEqualTo(1);
    }

    @Test
    void testNoDateInFilename() {
        Optional<DateInfo> dateInfo = DateInfo.fromFileName("photo.jpg");
        
        assertThat(dateInfo).isEmpty();
    }

    @Test
    void testDestinationPathForPhoto() {
        Optional<DateInfo> dateInfo = DateInfo.fromFileName("IMG_20240315.jpg");
        
        assertThat(dateInfo).isPresent();
        String destPath = dateInfo.get().getDestinationPath("IMG_20240315.jpg", null);
        assertThat(destPath).isEqualTo("2024/03/Photos/IMG_20240315.jpg");
    }

    @Test
    void testDestinationPathForVideo() {
        Optional<DateInfo> dateInfo = DateInfo.fromFileName("VID_20240315.mp4");
        
        assertThat(dateInfo).isPresent();
        String destPath = dateInfo.get().getDestinationPath("VID_20240315.mp4", null);
        assertThat(destPath).isEqualTo("2024/03/Videos/VID_20240315.mp4");
    }

    @Test
    void testDestinationPathWithUndatedFolder() {
        Optional<DateInfo> dateInfo = DateInfo.fromFileName("photo.jpg");
        
        assertThat(dateInfo).isEmpty();
        String destPath = "Undated/photo.jpg";
        
        assertThat(destPath).isEqualTo("Undated/photo.jpg");
    }

    @Test
    void testDestinationPathUsesPhotosFolderForPng() {
        Optional<DateInfo> dateInfo = DateInfo.fromFileName("2024-05-01-image.png");
        
        assertThat(dateInfo).isPresent();
        String destPath = dateInfo.get().getDestinationPath("2024-05-01-image.png", null);
        assertThat(destPath).isEqualTo("2024/05/Photos/2024-05-01-image.png");
    }
}
