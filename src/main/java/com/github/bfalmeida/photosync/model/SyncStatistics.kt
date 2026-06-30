package com.github.bfalmeida.photosync.model

import java.util.concurrent.atomic.AtomicInteger

class SyncStatistics {
    private val filesFound = AtomicInteger(0)
    private val copied = AtomicInteger(0)
    private val skipped = AtomicInteger(0)
    private val errors = AtomicInteger(0)

    fun incrementFound() { filesFound.incrementAndGet() }
    fun incrementCopied() { copied.incrementAndGet() }
    fun incrementSkipped() { skipped.incrementAndGet() }
    fun incrementErrors() { errors.incrementAndGet() }

    val filesFoundCount: Int get() = filesFound.get()
    val copiedCount: Int get() = copied.get()
    val skippedCount: Int get() = skipped.get()
    val errorsCount: Int get() = errors.get()

    override fun toString(): String {
        return "Found ${filesFound.get()} files: ${copied.get()} copied, ${skipped.get()} skipped, ${errors.get()} errors"
    }
}
