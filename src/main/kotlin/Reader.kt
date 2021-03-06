package bakiev.artour.simpledisklrucache

import java.io.Closeable
import java.io.InputStream

interface Reader : Closeable {

    fun open(): InputStream
}
