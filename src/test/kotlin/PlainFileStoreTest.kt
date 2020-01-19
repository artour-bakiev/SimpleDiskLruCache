package bakiev.artour.simpledisklrucache

import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be true`
import org.amshove.kluent.`should equal`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class PlainFileStoreTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()
    private lateinit var workingDirectory: File

    @Before
    fun setup() {
        workingDirectory = temporaryFolder.newFolder()
    }

    @Test
    fun `should properly create empty file store`() {
        val fileStore = PlainFileStore(workingDirectory)
        val lruCache = EntryLruCache(2_000_000)
        fileStore.readAll(lruCache)

        lruCache.size() `should be equal to` 0
        val logFile = File(workingDirectory, "FCDBAEE5-BD39-488F-99F1-C240C8E81FB9-log-file")
        logFile.exists().`should be true`()
        logFile.length() `should equal` 0
    }

    @Test
    fun `should read what has been added`() {
        val fileStore = PlainFileStore(workingDirectory)
        fileStore.addEntry("www.google.com/", Entry("A", 2340))
        val lruCache = EntryLruCache(2_000_000)
        fileStore.readAll(lruCache)

        lruCache.size() `should be equal to` 2340
        val d = lruCache.entries().next()
        d.key `should be equal to` "www.google.com/"
        d.value.fileName `should be equal to` "A"
        d.value.length `should be equal to` 2340
    }

    @Test
    fun `should remove what has been added`() {
        val fileStore = PlainFileStore(workingDirectory)
        fileStore.addEntry("www.google.com/", Entry("A", 2340))
        fileStore.removeEntry("www.google.com/")
        val lruCache = EntryLruCache(2_000_000)
        fileStore.readAll(lruCache)

        lruCache.size() `should be equal to` 0
    }

    @Test
    fun `should not be modified by removing a non existing key`() {
        val fileStore = PlainFileStore(workingDirectory)
        fileStore.addEntry("www.google.com/", Entry("A", 230))
        fileStore.removeEntry("www.google.com")
        val lruCache = EntryLruCache(2_000_000)
        fileStore.readAll(lruCache)

        lruCache.size() `should be equal to` 230
        val entry = lruCache.entries().next()
        entry.key `should be equal to` "www.google.com/"
        entry.value.fileName `should be equal to` "A"
        entry.value.length `should be equal to` 230
    }
}
