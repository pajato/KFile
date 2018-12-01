package com.pajato.io

import java.io.File

class KFileJvm(private val kFile: File?, dir: String, name: String, override val errors: String) : KFile {
    override val path: String = if (kFile != null) kFile.path else "$dir:$name"
    // This would be better (but for Jacoco shortcomings): kFile?.path ?: "$dir:$name"

    override fun appendText(line: String) {
        kFile?.appendText(line)
    }

    override fun clear() {
        kFile?.writeText("")
    }

    override fun close() {}

    override fun exists() = kFile?.isFile ?: false

    override fun forEachLine(action: (line: String) -> Unit) {
        kFile?.forEachLine { line -> action(line) }
    }

    override fun size(): Int {
        var sum = 0
        forEachLine { sum += it.length + 1}
        return sum
    }

    override fun readLines() = if (kFile != null) kFile.readLines() else listOf()
    // This would be better (but for Jacoco's shortcomings): kFile?.readLines() ?: listOf()
}

actual fun createKotlinFile(dir: String, name: String): KFile {
    val builder = StringBuilder()
    val baseDir: File? by lazy {
        fun badDirectory(message: String): File? {
            builder.append("Invalid directory: ($dir); $message")
            return null
        }
        val dirPath = if (dir.isNotEmpty() && !dir.endsWith('/')) "$dir/" else dir

        when {
            dir.isEmpty() -> File(".")
            File(dirPath).isDirectory -> File(dirPath)
            File(dirPath).isFile -> badDirectory("is a file!")
            else ->badDirectory("Directory ($dir) does not exist.")
        }
    }
    val kFile: File? by lazy {
        fun badFile(message: String): File? {
            builder.append("Invalid file: ($name); $message")
            return null
        }
        fun createFile(file: File): File? =
                try {
                    file.apply { createNewFile() }
                } catch (exc: java.io.IOException) {
                    badFile("could not create file: $exc")
                }

        baseDir?.run {
            when {
                name.isEmpty() -> badFile("The filename cannot be the empty string!")
                name.contains('/') || name.contains(Char.MIN_VALUE) -> badFile(illegalCharMessage)
                else -> createFile(File(dir, name))
            }

        }
    }

    return KFileJvm(kFile, dir, name, builder.toString())
}
