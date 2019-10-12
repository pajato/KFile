package com.pajato.io

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import platform.posix.EAGAIN
import platform.posix.EBADF
import platform.posix.EFBIG
import platform.posix.EINTR
import platform.posix.EIO
import platform.posix.ENOMEM
import platform.posix.ENOSPC
import platform.posix.ENXIO
import platform.posix.EPIPE
import platform.posix.FILE
import platform.posix.F_OK
import platform.posix.SEEK_SET
import platform.posix.access
import platform.posix.fclose
import platform.posix.fgets
import platform.posix.fopen
import platform.posix.fprintf
import platform.posix.fputs
import platform.posix.fseek
import platform.posix.ftell
import platform.posix.getcwd
import platform.posix.realpath
import platform.posix.rewind
import platform.posix.stderr
import platform.posix.truncate

class Result(val eof: Boolean = true, val value: String = "") {
    operator fun component1() = eof
    operator fun component2() = value
}

typealias File = CPointer<FILE>

@ExperimentalUnsignedTypes
actual fun createKotlinFile(dir: String, name: String): KFile {
    val builder = StringBuilder()
    val dirPath: String? by lazy {
        when {
            dir.isEmpty() -> "./".realPath
            dir.endsWith('/') -> dir.realPath
            else -> "$dir/".realPath
        }
    }
    val kFile: File? by lazy {
        fun badFile(message: String): File? {
            builder.append("Invalid file: ($name); $message")
            return null
        }

        if (dirPath == null)  badFile("the directory \"$dir\" does not exist!\n")
        dirPath?.run {
            when {
                name.isEmpty() -> badFile("The filename cannot be the empty string!\n")
                name.contains('/') || name.contains(Char.MIN_VALUE) -> badFile(illegalCharMessage)
                else -> fopen("${this@run}/$name", "a+") ?: badFile("could not create (${this@run}/$name.")
            }
        }
    }

    val finalDir = if (dirPath != null) "$dirPath/" else "$dir:"
    return KFileNative(kFile, finalDir, name, builder.toString())
}

class KFileNative(private val file: File?, dir: String, name: String, override val errors: String) : KFile {

    override val path = "$dir$name"

    override fun appendText(line: String) {
        try {
            if (line.isEmpty()) return
            val result = fputs(line, file)
            if (result >= 0) return
            println("appendText failed: ${result.toError()}")
        } catch (exc: Exception) {
            exc.printStackTrace()
        }
    }

    override fun clear() {
        if (file == null) return
        if (truncate(path, 0) < 0) fprintf(stderr, "Could not clear the file at ($path)")
    }

    override fun close() {
        if (file != null) fclose(file)
    }

    override fun exists() = file != null

    override fun forEachLine(action: (line: String) -> Unit) {
        fun processNextLine(): Result {
            fun readLine(): Result {
                fun nextLine(): Result {
                    val buffer = ByteArray(256)
                    val nextLine = fgets(buffer.refTo(0), buffer.size, file)?.toKString()
                    return if (nextLine == null) Result() else Result(false, nextLine)
                }

                val line = StringBuilder()
                while (true) {
                    val result = nextLine()
                    if (result.eof) return result
                    line.append(result.value)
                    if (result.value.endsWith("\n")) return Result(false, line.toString())
                }
            }

            while (true) {
                val (eof, line) = readLine()
                if (eof) return Result()
                action(line)
            }
        }

        if (file == null) return
        val position = ftell(file)
        rewind(file)
        while (true) {
            val result = processNextLine()
            if (result.eof) break
        }
        fseek(file, position, SEEK_SET)
    }

    override fun size(): Int {
        if (file == null) return -1
        var sum = 0
        forEachLine {
            sum += it.length
        }
        return sum
    }

    override fun readLines(): List<String> {
        if (file == null) return listOf()
        val list = mutableListOf<String>()

        forEachLine {
            list.add(it)
        }
        return list
    }
}

@ExperimentalUnsignedTypes
val String.realPath: String?
    get() = memScoped {
        val buffer = ByteArray(4096)
        realpath(this@realPath, buffer.refTo(0))?.toKString()
    }

// fputs() error messages.
const val MessageEAGAIN = "The O_NONBLOCK flag is set for the file descriptor underlying stream and the thread would be delayed in the write operation."
const val MessageEBADF = "The file descriptor underlying stream is not a valid file descriptor open for writing."
const val MessageEFBIG = "An attempt was made to write to a file that exceeds the maximum file size, or the file is a regular file and an attempt was made to write at or beyond the offset maximum."
const val MessageEINTR = "The write operation was terminated due to the receipt of a signal, and no data was transferred."
const val MessageEIO = "A physical I/O error has occurred, or the process is a member of a background process group attempting to write to its controlling terminal, TOSTOP is set, the calling thread is not blocking SIGTTOU, the process is not ignoring SIGTTOU, and the process group of the process is orphaned. This error may also be returned under implementation-defined conditions."
const val MessageENOSPC = "There was no free space remaining on the device containing the file."
const val MessageEPIPE = "An attempt is made to write to a pipe or FIFO that is not open for reading by any process. A SIGPIPE signal shall also be sent to the thread."
const val MessageENOMEM = "Insufficient storage space is available."
const val MessageENXIO = "A request was made of a nonexistent device, or the request was outside the capabilities of the device."

fun Int.toError(): String {
    return when (this@toError) {
        EAGAIN -> MessageEAGAIN
        EBADF -> MessageEBADF
        EFBIG -> MessageEFBIG
        EINTR -> MessageEINTR
        EIO -> MessageEIO
        ENOSPC -> MessageENOSPC
        EPIPE -> MessageEPIPE
        ENOMEM -> MessageENOMEM
        ENXIO -> MessageENXIO
        else -> "Unrecognized error code: ${this@toError}."
    }
}

/**
 * Create a Kotlin file given a file URL (all other scheme's are unsupported and will result in an error).
 */
@ExperimentalUnsignedTypes
actual fun createKFileWithUrl(url: String): KFile {
    fun getPath(): String {
        val prefix = "file://localhost"
        return if (url.startsWith(prefix)) url.substring(prefix.length) else ""
    }
    fun convertToKFile(path: String): KFile {
        val fileNameStartIndex = path.lastIndexOf('/')
        val dir = path.substring(0, fileNameStartIndex)
        val name = path.substring(fileNameStartIndex + 1)
        println("dir: $dir; name: $name")
        return createKotlinFile(dir, name)
    }
    val path = getPath()

    return when {
        path.isNotEmpty() -> convertToKFile(path)
        else -> KFileNative(null, "", "", "Invalid URL scheme: $url")
    }
}

/** Get the current working directory. */
actual fun getWorkingDirectory(): String {
    val cwd = ByteArray(1024)

    cwd.usePinned { getcwd(it.addressOf(0), 1024) }
    return cwd.toKString()
}