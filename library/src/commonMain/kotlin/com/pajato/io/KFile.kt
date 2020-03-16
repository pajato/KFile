package com.pajato.io

interface KFile {
    val path: String
    val errors: String
    fun appendText(line: String)
    fun clear()
    fun close()
    fun exists(): Boolean
    fun forEachLine(action: (line: String) -> Unit)
    fun size(): Int
    fun readLines(): List<String>
}

const val illegalCharMessage = "Illegal characters (/ or null) in file name.\n"

/**
 * Create a Kotlin file given a directory DIR and a name NAME. An error will be logged if the directory does not exist.
 * If DIR is empty the current directory is used. NAME is checked for invalid characters (/ and null). The errors
 * property will be the empty string for no errors and a collection of error strings otherwise.
 */
expect fun createKotlinFile(dir: String = "", name: String): KFile

/** Create a Kotlin file given a file URL (all other scheme's are unsupported and will result in an error). */
expect fun createKFileWithUrl(url: String): KFile

/** Get the current working directory. */
expect fun getWorkingDirectory(): String
