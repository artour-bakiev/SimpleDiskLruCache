package bakiev.artour.simpledisklrucache

import java.io.*
import java.util.*

class PlainFileStore(workingDirectory: File) : Store {

    private val logFile = File(workingDirectory, LOG_FILE_NAME)
    private var logWriter: PrintWriter = PrintWriter(BufferedWriter(FileWriter(logFile, true)))

    override fun init(entries: Iterator<Map.Entry<String, Entry>>) {
        logWriter = PrintWriter(BufferedWriter(FileWriter(logFile)))
        entries.forEach {
            logPutOperation(it.key, it.value)
        }
        logWriter.flush()
    }

    override fun readAll(lruCache: EntryLruCache) {
        if (!logFile.exists()) {
            return
        }

        val reader = BufferedReader(FileReader(logFile))
        reader.use {
            var line = it.readLine()
            while (line != null) {
                readLogLine(line, lruCache)
                line = it.readLine()
            }
        }
    }

    override fun removeEntry(key: String) {
        logRemoveOperation(key)
        logWriter.flush()
    }

    override fun addEntry(key: String, entry: Entry) {
        logPutOperation(key, entry)
        logWriter.flush()
    }

    override fun close() = logWriter.close()

    private fun readLogLine(line: String, lruCache: LruCache<String, Entry>) {
        val tokenizer = StringTokenizer(line, DELIMITER)

        if (!tokenizer.hasMoreElements()) return

        val operation = tokenizer.nextToken()

        if (!tokenizer.hasMoreTokens()) return

        val key = tokenizer.nextToken()
        if (operation == PUT) {
            if (!tokenizer.hasMoreTokens()) return
            val fileName = tokenizer.nextToken()
            if (!tokenizer.hasMoreTokens()) return
            val length = tokenizer.nextToken().toLong()
            lruCache.put(key, Entry(fileName, length))
        } else if (operation == REMOVE) {
            lruCache.remove(key)
        }
    }

    private fun logPutOperation(key: String, entry: Entry?) {
        entry ?: return
        logWriter.println(PUT + DELIMITER + key + DELIMITER + entry.fileName + DELIMITER + entry.length.toString())
    }

    private fun logRemoveOperation(key: String) = logWriter.println(REMOVE + DELIMITER + key)

    companion object {
        private const val LOG_FILE_NAME = "FCDBAEE5-BD39-488F-99F1-C240C8E81FB9-log-file"
        private const val PUT = "PUT"
        private const val REMOVE = "REMOVE"
        private const val DELIMITER = "|"
    }
}