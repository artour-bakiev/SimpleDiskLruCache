package bakiev.artour.simpledisklrucache

interface Store {

    fun readAll(lruCache: EntryLruCache)

    fun init(entries: Iterator<Map.Entry<String, Entry>>)

    fun removeEntry(key: String)

    fun addEntry(key: String, entry: Entry)

    fun close()
}
