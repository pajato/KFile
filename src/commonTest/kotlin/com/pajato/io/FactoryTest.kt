package com.pajato.io

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Provide a class to test the KFile factory function.
 */
class FactoryTest {
    private val tempDir = "build/"
    private val emptyFileDirWithoutFileSeparator = "build"
    private val emptyFileName = "factory-test-empty.txt"
    private val oneLineName = "factory-test-one-line.txt"

    private fun runTest(dir: String = "", name: String = "", content: String = "", test: (uut: KFile) -> Unit) {
        val uut = createKotlinFile(dir, name)
        if (content.isNotEmpty()) {
            uut.clear()
            uut.appendText(content)
        }
        test(uut)
        uut.close()
    }

    @Test
    fun `when all defaults are used verify that an error is generated`() {
        runTest { uut: KFile ->
            assertTrue(uut.errors.isNotEmpty())
            uut.appendText("")
            uut.clear()
            assertFalse(uut.exists(), "The file should not exist!")
            uut.forEachLine {  }
            assertEquals(listOf(), uut.readLines())
        }
    }

    @Test
    fun `when an empty text file is created verify that it exists`() {
        runTest(this.tempDir, emptyFileName) { uut: KFile ->
            assertTrue(uut.exists(), "$emptyFileName could not be created in ${this.tempDir}!")
        }
    }

    @Test
    fun `when an empty text file is created without file separator verify that it exists`() {
        runTest(emptyFileDirWithoutFileSeparator, emptyFileName) {uut: KFile ->
            assertTrue(uut.exists())
        }
    }

    @Test
    fun `when an empty text file is created verify that the path is correct`() {
        runTest(this.tempDir, emptyFileName) { uut: KFile ->
            assertTrue(uut.path.endsWith("${tempDir}factory-test-empty.txt"), "The actual path is wrong!")
        }
    }

    @Test
    fun `when an empty text file is created verify that it's size is 0`() {
        runTest(this.tempDir, emptyFileName) { uut: KFile ->
            assertEquals(0, uut.size())
        }
    }

    @Test
    fun `when an empty text file is created verify that it's list size is 0`() {
        runTest(this.tempDir, emptyFileName) { uut: KFile ->
            assertEquals(0, uut.readLines().size)
        }
    }

    @Test
    fun `when a text file with one line is created verify that it's size is greater than 0`() {
        val content = "A test line.\n"
        runTest(this.tempDir, oneLineName, content) { uut: KFile ->
            assertEquals(content.length, uut.size())
        }
    }

    @Test
    fun `when an invalid directory is specified verify that an error is generated`() {
        val dir = "src/commonTest/resources/untitled.txt"
        runTest(dir) { uut: KFile ->
            assertTrue(uut.errors.isNotEmpty())
        }
    }

    @Test
    fun `when a directory is specified that does not exist verify that an error is generated`() {
        runTest("build/tmp/doesNotExistDir") { uut: KFile ->
            assertTrue(uut.errors.isNotEmpty())
        }
    }

    @Test
    fun `when a file with an illegal null character is created verify that an error is generated`() {
        runTest(tempDir, "foo" + Char.MIN_VALUE + "fee") { uut: KFile ->
            assertTrue(uut.errors.isNotEmpty(), "A null character in the name was accepted incorrectly!")
        }
    }

    @Test
    fun `when a file with an illegal forward slash character is created verify that an error is generated`() {
        runTest(tempDir, "abc/def") { uut: KFile ->
            assertTrue(uut.errors.isNotEmpty(), "A / character in the name was accepted incorrectly!")
        }
    }

    @Test
    fun `when a valid file is created in a read-only directory verify that an error is generated`() {
        runTest("/", "fred") { uut: KFile ->
            assertTrue(uut.errors.isNotEmpty(), "An attempt to create the file in root (/) did not fail!")
        }
    }
}
