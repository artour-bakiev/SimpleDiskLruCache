package bakiev.artour.simpledisklrucache

open class EntryLruCache : LruCache<String, Entry> {

    constructor(other: EntryLruCache) : super(other)

    constructor(maxSize: Long) : super(maxSize)

    final override fun sizeOf(key: String, value: Entry): Long = value.length
}
