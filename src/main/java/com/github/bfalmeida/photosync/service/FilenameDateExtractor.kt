package com.github.bfalmeida.photosync.service

import org.springframework.stereotype.Component
import java.time.YearMonth
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Matcher
import java.util.regex.Pattern

@Component
class FilenameDateExtractor {

    companion object {
        private val YYYY_MM_DD_HH_MM_SS_PATTERN =
            Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})_(\\d{2})-(\\d{2})-(\\d{2})")
        private val IMG_YYYYMMDD_WA_PATTERN =
            Pattern.compile("IMG-(\\d{4})(\\d{2})(\\d{2})-WA(\\d{4})")
        private val IMG_YYYYMMDD_HHMMSS_PATTERN =
            Pattern.compile("IMG_(\\d{4})(\\d{2})(\\d{2})_(\\d{2})(\\d{2})(\\d{2})")
        private val VID_YYYYMMDD_HHMMSS_PATTERN =
            Pattern.compile("VID_(\\d{4})(\\d{2})(\\d{2})_(\\d{2})(\\d{2})(\\d{2})")
    }

    private val cache: MutableMap<String, Optional<DateInfo>> = Collections.synchronizedMap(
        object : LinkedHashMap<String, Optional<DateInfo>>(100, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Optional<DateInfo>>): Boolean {
                return size > 1000
            }
        }
    )

    fun extract(fileName: String?): Optional<DateInfo> {
        if (fileName.isNullOrEmpty()) {
            return Optional.empty()
        }

        return cache.computeIfAbsent(fileName) { doExtract(it) }
    }

    private fun doExtract(fileName: String): Optional<DateInfo> {
        val yyyyMmDdMatcher = YYYY_MM_DD_HH_MM_SS_PATTERN.matcher(fileName)
        if (yyyyMmDdMatcher.find()) {
            return Optional.of(DateInfo(
                yyyyMmDdMatcher.group(1).toInt(),
                yyyyMmDdMatcher.group(2).toInt(),
                false
            ))
        }

        val imgWaMatcher = IMG_YYYYMMDD_WA_PATTERN.matcher(fileName)
        if (imgWaMatcher.find()) {
            return Optional.of(DateInfo(
                imgWaMatcher.group(1).toInt(),
                imgWaMatcher.group(2).toInt(),
                true
            ))
        }

        val imgMatcher = IMG_YYYYMMDD_HHMMSS_PATTERN.matcher(fileName)
        if (imgMatcher.find()) {
            return Optional.of(DateInfo(
                imgMatcher.group(1).toInt(),
                imgMatcher.group(2).toInt(),
                false
            ))
        }

        val vidMatcher = VID_YYYYMMDD_HHMMSS_PATTERN.matcher(fileName)
        if (vidMatcher.find()) {
            return Optional.of(DateInfo(
                vidMatcher.group(1).toInt(),
                vidMatcher.group(2).toInt(),
                false
            ))
        }

        return Optional.empty()
    }

    fun clearCache() {
        cache.clear()
    }

    class DateInfo(val year: Int, val month: Int, val isWhatsApp: Boolean) {
        fun getYearMonth(): YearMonth {
            return YearMonth.of(year, month)
        }
    }
}
