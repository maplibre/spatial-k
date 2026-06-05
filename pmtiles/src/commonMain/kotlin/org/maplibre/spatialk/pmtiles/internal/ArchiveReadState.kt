@file:OptIn(kotlin.concurrent.atomics.ExperimentalAtomicApi::class)

package org.maplibre.spatialk.pmtiles.internal

import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicReference
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.io.bytestring.ByteString
import org.maplibre.spatialk.pmtiles.ArchiveMetadata
import org.maplibre.spatialk.pmtiles.ArchiveWarning
import org.maplibre.spatialk.pmtiles.ArchiveWarningCode
import org.maplibre.spatialk.pmtiles.ByteRange
import org.maplibre.spatialk.pmtiles.ByteRangeSource
import org.maplibre.spatialk.pmtiles.PmTilesErrorCode
import org.maplibre.spatialk.pmtiles.PmTilesException

internal class ArchiveReadState(initialWarnings: List<ArchiveWarning>) {
    private val stateMutex = Mutex()
    private val closed = AtomicBoolean(false)
    private val archiveWarnings = AtomicReference(WarningState.from(initialWarnings))
    private val leafDirectoryCache = LinkedHashMap<ByteRange, List<DirectoryEntry>>()
    private val inFlightSourceReads = mutableMapOf<SourceReadKey, CompletableDeferred<ByteString>>()
    private var rawMetadataJsonCache: String? = null
    private var metadataCache: ArchiveMetadata? = null

    fun warnings(): List<ArchiveWarning> = archiveWarnings.load().warnings

    fun close() {
        if (!closed.compareAndSet(expectedValue = false, newValue = true)) return
        if (stateMutex.tryLock()) {
            try {
                clearStateForCloseLocked()
            } finally {
                stateMutex.unlock()
            }
        }
    }

    fun checkOpen() {
        if (closed.load()) throw closedException()
    }

    suspend fun cachedRawMetadataJson(): String? = withOpenStateLock {
        rawMetadataJsonCache
    }

    suspend fun cacheRawMetadataJson(json: String): String = withOpenStateLock {
        rawMetadataJsonCache ?: json.also { rawMetadataJsonCache = it }
    }

    suspend fun cachedMetadata(): ArchiveMetadata? = withOpenStateLock {
        metadataCache
    }

    suspend fun cacheMetadata(metadata: ArchiveMetadata): ArchiveMetadata = withOpenStateLock {
        metadataCache?.let {
            return@withOpenStateLock it
        }

        metadata.also { metadataCache = it }
    }

    suspend fun cachedLeafDirectory(range: ByteRange): List<DirectoryEntry>? = withOpenStateLock {
        val cached = leafDirectoryCache.remove(range) ?: return@withOpenStateLock null
        leafDirectoryCache[range] = cached
        cached
    }

    suspend fun cacheLeafDirectory(
        range: ByteRange,
        directory: List<DirectoryEntry>,
        maxEntries: Int,
    ): List<DirectoryEntry> = withOpenStateLock {
        leafDirectoryCache.remove(range)?.let { cached ->
            leafDirectoryCache[range] = cached
            return@withOpenStateLock cached
        }
        if (maxEntries > 0) {
            leafDirectoryCache[range] = directory
            while (leafDirectoryCache.size > maxEntries) {
                leafDirectoryCache.remove(leafDirectoryCache.keys.first())
            }
        }
        directory
    }

    suspend fun readSourceRangeDeduplicated(
        source: ByteRangeSource,
        archiveSize: ULong,
        range: ByteRange,
        maxBytes: ULong,
    ): ByteString {
        val key = SourceReadKey(range = range, maxBytes = maxBytes)
        var ownsRead = false
        val inFlight = withOpenStateLock {
            inFlightSourceReads[key]?.let {
                return@withOpenStateLock it
            }
            CompletableDeferred<ByteString>().also {
                inFlightSourceReads[key] = it
                ownsRead = true
            }
        }

        if (!ownsRead) return inFlight.await()

        return try {
            val bytes =
                source.readSourceRange(
                    range,
                    archiveSize = archiveSize,
                    maxBytes = maxBytes,
                )
            completeSourceRead(key, inFlight, bytes)
            bytes
        } catch (error: Throwable) {
            val completionError = failSourceRead(key, inFlight, error)
            throw completionError
        }
    }

    fun appendWarning(warning: ArchiveWarning) {
        val key = warning.dedupeKey
        while (true) {
            val current = archiveWarnings.load()
            if (key in current.keys) return
            val next =
                WarningState(warnings = current.warnings + warning, keys = current.keys + key)
            if (archiveWarnings.compareAndSet(current, next)) return
        }
    }

    private suspend fun completeSourceRead(
        key: SourceReadKey,
        inFlight: CompletableDeferred<ByteString>,
        bytes: ByteString,
    ) {
        withStateLock {
            inFlightSourceReads.remove(key)
            if (closed.load()) {
                clearStateForCloseLocked()
                val error = closedException()
                inFlight.completeExceptionally(error)
                throw error
            }
            inFlight.complete(bytes)
        }
    }

    private suspend fun failSourceRead(
        key: SourceReadKey,
        inFlight: CompletableDeferred<ByteString>,
        error: Throwable,
    ): Throwable =
        withContext(NonCancellable) {
            withStateLock {
                inFlightSourceReads.remove(key)
                val completionError =
                    if (closed.load() && error !is CancellationException) {
                        closedException()
                    } else {
                        error
                    }
                if (closed.load()) clearStateForCloseLocked()
                inFlight.completeExceptionally(completionError)
                completionError
            }
        }

    private suspend fun <T> withOpenStateLock(block: () -> T): T = withStateLock {
        checkOpenLocked()
        block()
    }

    private suspend fun <T> withStateLock(block: () -> T): T = stateMutex.withLock {
        try {
            block()
        } finally {
            if (closed.load()) clearStateForCloseLocked()
        }
    }

    private fun checkOpenLocked() {
        if (closed.load()) {
            clearStateForCloseLocked()
            throw closedException()
        }
    }

    private fun clearStateForCloseLocked() {
        rawMetadataJsonCache = null
        metadataCache = null
        leafDirectoryCache.clear()
        val error = closedException()
        inFlightSourceReads.values.forEach { it.completeExceptionally(error) }
        inFlightSourceReads.clear()
    }

    private fun closedException(): PmTilesException =
        pmTilesException(PmTilesErrorCode.Closed, "PMTiles archive is closed.")
}

private data class SourceReadKey(
    val range: ByteRange,
    val maxBytes: ULong,
)

private val ArchiveWarning.dedupeKey: WarningDedupeKey
    get() = WarningDedupeKey(code = code, context = context)

private data class WarningState(
    val warnings: List<ArchiveWarning>,
    val keys: Set<WarningDedupeKey>,
) {
    companion object {
        fun from(warnings: List<ArchiveWarning>): WarningState {
            val dedupedWarnings = mutableListOf<ArchiveWarning>()
            val keys = mutableSetOf<WarningDedupeKey>()
            warnings.forEach { warning ->
                if (keys.add(warning.dedupeKey)) dedupedWarnings += warning
            }
            return WarningState(warnings = dedupedWarnings.toList(), keys = keys.toSet())
        }
    }
}

private data class WarningDedupeKey(
    val code: ArchiveWarningCode,
    val context: String?,
)
