package com.github.bfalmeida.photosync.model;

/**
 * Media types supported for synchronization.
 */
public enum MediaType {
    /**
     * Photo image files (JPEG, PNG, HEIC, RAW, etc.).
     */
    PHOTO,
    /**
     * Video files (MP4, MOV, AVI, etc.).
     */
    VIDEO,
    /**
     * Unknown or unsupported media type.
     */
    UNKNOWN
}