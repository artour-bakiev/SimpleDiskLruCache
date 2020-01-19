package bakiev.artour.simpledisklrucache

import org.amshove.kluent.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.*
import kotlin.math.abs

class FileDiskLruCacheTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()
    private lateinit var workingDirectory: File

    @Before
    fun setup() {
        workingDirectory = temporaryFolder.newFolder()
    }

    @Test
    fun `should return null from read if key doesn't exist`() {
        val lruCache = FileDiskLruCache(workingDirectory, 1_000)

        lruCache.read("A").`should be null`()
    }

    @Test
    fun `should read what has been written`() {
        val lruCache = FileDiskLruCache(workingDirectory, 1_000)
        val sampleBuffer = ByteArray(20) { n -> (n * 2).toByte() }
        lruCache.write("A").flush(sampleBuffer)

        lruCache.read("A").`should be equal to`(sampleBuffer)
    }

    @Test
    fun `should properly handle reopening`() {
        val lruCache = FileDiskLruCache(workingDirectory, 1_000)
        val sampleBufferForKeyA = ByteArray(20) { n -> (n * 2).toByte() }
        lruCache.write("A").flush(sampleBufferForKeyA)
        val sampleBufferForKeyB = ByteArray(30) { n -> (n * 3).toByte() }
        lruCache.write("B").flush(sampleBufferForKeyB)
        lruCache.close()

        val lruCache2 = FileDiskLruCache(workingDirectory, 1_000)
        lruCache2.read("A").`should be equal to`(sampleBufferForKeyA)
        lruCache2.read("B").`should be equal to`(sampleBufferForKeyB)
    }

    @Test
    fun `should respect storage space limit`() {
        val lruCache = FileDiskLruCache(workingDirectory, 19)
        lruCache.write("A").flush(ByteArray(20))

        lruCache.read("A").`should be null`()
    }

    @Test
    fun `should displace oldest`() {
        val lruCache = FileDiskLruCache(workingDirectory, 49)
        lruCache.write("A").flush(ByteArray(20))
        val sampleBufferForKeyB = ByteArray(30) { n -> (n * 3).toByte() }
        lruCache.write("B").flush(sampleBufferForKeyB)

        lruCache.read("A").`should be null`()
        lruCache.read("B").`should be equal to`(sampleBufferForKeyB)
    }

    @Test
    fun `should properly handle storage space reducing`() {
        val lruCache1 = FileDiskLruCache(workingDirectory, 50)
        val sampleBufferA = ByteArray(45) { n -> (n).toByte() }
        val keyA = "http://google.com?param=34&value=30"
        lruCache1.write(keyA).flush(sampleBufferA)
        val sampleBufferB = ByteArray(5) { n -> (n * 3).toByte() }
        val keyB = "http://google.com?param=34&value=31"
        lruCache1.write(keyB).flush(sampleBufferB)

        lruCache1.read(keyA).`should not be null`()
        lruCache1.read(keyB).`should not be null`()

        lruCache1.close()
        val lruCache2 = FileDiskLruCache(workingDirectory, 49)
        val keyAFound = lruCache2.read(keyA) != null
        val keyBFound = lruCache2.read(keyB) != null

        keyAFound `should not be equal to` keyBFound
        lruCache2.read(keyA)?.`should be equal to`(sampleBufferA)
        lruCache2.read(keyB)?.`should be equal to`(sampleBufferB)
        lruCache2.read(keyA) `should not equal` lruCache2.read(keyB)
    }

    @Test
    fun `should handle monkey multithreading test`() {
        val numberOfThreads = 100
        val singleBufferSize = 20
        val lruCache = FileDiskLruCache(workingDirectory, numberOfThreads * singleBufferSize.toLong())
        val threads = mutableListOf<Thread>()
        val r = Random()
        val errors = Collections.synchronizedList(mutableListOf<String>())
        val buffers =
            MutableList(numberOfThreads) { n -> ByteArray(singleBufferSize) { n.toByte() } }
        for (n in 0 until numberOfThreads) {
            val t = Thread(Runnable {
                try {
                    Thread.sleep(abs(r.nextLong()) % 500)
                    lruCache.write(n.toString()).flush(buffers[n])
                    Thread.sleep(abs(r.nextLong()) % 500)
                    lruCache.read(n.toString()).`should be equal to`(buffers[n])
                } catch (t: Throwable) {
                    errors.add(t.message)
                }
            })
            threads.add(t)
            t.start()
        }
        for (t in threads) {
            t.join()
        }

        errors.`should be empty`()
    }

    private fun Writer.flush(buffer: ByteArray) = use {
        it.open().use { outputStream ->
            outputStream.write(buffer)
        }
    }

    private fun Reader?.`should be equal to`(buffer: ByteArray) {
        `should not be null`()
        use {
            it?.open().use { inputStream ->
                val read = ByteArray(buffer.size)
                inputStream?.read(read)?.`should be equal to`(buffer.size)
                read.`should equal`(buffer)
                inputStream?.read()?.`should be equal to`(-1)
            }
        }
    }
}