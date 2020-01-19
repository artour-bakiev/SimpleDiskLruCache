package bakiev.artour.simpledisklrucache

import java.util.*

abstract class LruCache<K, V> private constructor(
    private val map: LinkedHashMap<K, V>,
    private val maxSize: Long,
    private var currentSize: Long
) {

    constructor(maxSize: Long) : this(LinkedHashMap<K, V>(0, 0.75f, true), maxSize, 0)

    constructor(other: LruCache<K, V>) : this(other.map, other.maxSize, other.currentSize)

    init {
        require(maxSize > 0) { "maxSize <= 0" }
    }

    operator fun get(key: K): V? = map[key]

    fun entries(): Iterator<Map.Entry<K, V>> = map.entries.iterator()

    fun put(key: K, value: V): V? {
        currentSize += safeSizeOf(key, value)
        val previous = map.put(key, value)
        if (previous != null) {
            currentSize -= safeSizeOf(key, previous)
        }

        if (previous != null) {
            onEntryRemoved(false, key, previous, value)
        }

        trimToSize(maxSize)
        return previous
    }

    fun remove(key: K): V? {
        val previous = map.remove(key)
        if (previous != null) {
            currentSize -= safeSizeOf(key, previous)
        }

        if (previous != null) {
            onEntryRemoved(false, key, previous, null)
        }

        return previous
    }

    fun size(): Long = currentSize

    abstract fun sizeOf(key: K, value: V): Long

    protected open fun onEntryRemoved(evicted: Boolean, key: K, oldValue: V, newValue: V?) {}

    private fun trimToSize(maxSize: Long) {
        while (true) {
            if (currentSize < 0 || map.isEmpty() && currentSize != 0L) {
                throw IllegalStateException(javaClass.name + ".sizeOf() is reporting inconsistent results!")
            }

            if (currentSize <= maxSize || map.isEmpty()) {
                return
            }

            val toEvict = map.entries.iterator().next()
            val key = toEvict.key
            val value = toEvict.value
            map.remove(key)
            currentSize -= safeSizeOf(key, value)

            onEntryRemoved(true, key, value, null)
        }
    }

    private fun safeSizeOf(key: K, value: V): Long {
        val result = sizeOf(key, value)
        return if (result < 0) {
            throw IllegalStateException("Negative size: $key=$value")
        } else {
            result
        }
    }

    override fun toString(): String = "LruCache[maxSize=$maxSize, size=$currentSize]"
}
