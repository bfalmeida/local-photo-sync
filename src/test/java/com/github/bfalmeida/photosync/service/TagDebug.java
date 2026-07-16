package com.github.bfalmeida.photosync.service;

import com.drew.metadata.mp4.Mp4Directory;
import com.drew.metadata.mp4.media.Mp4MediaDirectory;

public class TagDebug {
    public static void main(String[] args) {
        System.out.println("Mp4Directory.TAG_CREATION_TIME = " + Mp4Directory.TAG_CREATION_TIME);
        System.out.println("Mp4MediaDirectory.TAG_CREATION_TIME = " + Mp4MediaDirectory.TAG_CREATION_TIME);
    }
}