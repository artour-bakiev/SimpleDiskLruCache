package bakiev.artour.simpledisklrucache

import java.io.File

class FileDiskLruCache(workingDirectory: File, maxDiskStorageSpaceInBytes: Long) {
    private val cache: DiskLruCache =
        DiskLruCache(workingDirectory, PlainFileStore(workingDirectory), maxDiskStorageSpaceInBytes)

    /**
     * reader.use {
     *     ...
     *     it.open().use { inputStream ->
     *         ...
     *         inputStream.read(...)
     *     }
     * }
     */
    fun read(key: String): Reader? = cache.read(key)

    /**
     * Transaction pattern - OutputStream::flush commits transaction
     * writer.use {
     *     ...
     *     it.open().use { outputStream ->
     *         ...
     *         outputStream.write(...)
     *         // OutputStream::flush is mandatory to complete write transaction
     *         // But it's called automatically when the stream is closed
     *         // it.flush()
     *     }
     * }
     */
    fun write(key: String): Writer = cache.write(key)

    fun close() = cache.close()
}
