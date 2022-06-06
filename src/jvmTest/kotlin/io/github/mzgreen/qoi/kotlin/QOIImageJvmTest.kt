/*
 * MIT License
 *
 * Copyright (c) 2022 mzgreen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.mzgreen.qoi.kotlin

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class QOIImageJvmTest {

    private val reader = QOIReader()
    private val writer = QOIWriter()
    private val testFilePath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "test.qoi".toPath()

    @AfterTest
    fun tearDown() {
        if (FILE_SYSTEM.exists(testFilePath)) {
            FILE_SYSTEM.delete(testFilePath)
        }
        assertFalse(FILE_SYSTEM.exists(testFilePath))
    }

    @Test
    fun testReadWriteAllTestImages() {
        FILE_SYSTEM.list("qoi_test_images".toPath()).forEach { testImagePath ->
            // given
            val qoiImage = reader.read(testImagePath)

            // when
            writer.write(qoiImage, testFilePath)

            // then
            assertFilesEqual(testImagePath, testFilePath)
        }
    }

    private fun assertFilesEqual(path1: Path, path2: Path) {
        val file1 = FILE_SYSTEM.openReadOnly(path1)
        val file2 = FILE_SYSTEM.openReadOnly(path2)

        try {
            assertEquals(file1.size(), file2.size())

            file1.source().buffer().use { file1Source ->
                file2.source().buffer().use { file2Source ->
                    while (!file1Source.exhausted()) {
                        assertEquals(file1Source.readByte(), file2Source.readByte())
                    }
                }
            }
        } finally {
            file1.close()
            file2.close()
        }
    }
}
