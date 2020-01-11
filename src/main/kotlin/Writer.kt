package bakiev.artour.simpledisklrucache

import java.io.Closeable
import java.io.OutputStream

interface Writer : Closeable {

    fun open(): OutputStream
}
