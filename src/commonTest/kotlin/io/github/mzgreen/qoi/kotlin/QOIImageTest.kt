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

import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class QOIImageTest {

    private val fileSystem = FakeFileSystem()
    private val reader = QOIReader(fileSystem)
    private val writer = QOIWriter(fileSystem)
    private val testFileName = "test.qoi"

    @AfterTest
    fun tearDown() {
        if (fileSystem.exists(testFileName.toPath())) {
            fileSystem.delete(testFileName.toPath())
        }
        fileSystem.checkNoOpenFiles()
        assertFalse(fileSystem.exists(testFileName.toPath()))
    }

    @Test
    fun testWriteImageUsingSinkReadUsingSource() {
        // given
        val originalImg =
            QOIImage(IntArray(16 * 16 * QOIColorModel.RGB.channels), 16, 16, QOIColorModel.RGB, QOIColorSpace.SRGB)
        val path = testFileName.toPath()
        val fileHandle = fileSystem.openReadWrite(path)

        // when
        writer.write(originalImg, fileHandle.sink())
        val readImg = reader.read(fileHandle.source())
        fileHandle.close()

        // then
        assertEquals(originalImg, readImg)
    }

    @Test
    fun testWriteReadImageUsingFileHandle() {
        // given
        val originalImg =
            QOIImage(IntArray(16 * 16 * QOIColorModel.RGB.channels), 16, 16, QOIColorModel.RGB, QOIColorSpace.SRGB)
        val path = testFileName.toPath()
        val fileHandle = fileSystem.openReadWrite(path)

        // when
        writer.write(originalImg, fileHandle)
        val readImg = reader.read(fileHandle)
        fileHandle.close()

        // then
        assertEquals(originalImg, readImg)
    }

    @Test
    fun testWriteReadImageUsingOkioPath() {
        // given
        val originalImg =
            QOIImage(IntArray(16 * 16 * QOIColorModel.RGB.channels), 16, 16, QOIColorModel.RGB, QOIColorSpace.SRGB)
        val path = testFileName.toPath()

        // when
        writer.write(originalImg, path)
        val readImg = reader.read(path)

        // then
        assertEquals(originalImg, readImg)
    }

    @Test
    fun testWriteReadImageUsingStringPath() {
        // given
        val originalImg =
            QOIImage(IntArray(16 * 16 * QOIColorModel.RGB.channels), 16, 16, QOIColorModel.RGB, QOIColorSpace.SRGB)
        val path = testFileName

        // when
        writer.write(originalImg, path)
        val readImg = reader.read(path)

        // then
        assertEquals(originalImg, readImg)
    }

    @Test
    fun testWriteReadRandomRGBImage() {
        // given
        val width = 11
        val height = 31
        val colorModel = QOIColorModel.RGB
        val colors = Random.nextBytes(width * height * colorModel.channels).map { it.toInt() and 0xFF }.toIntArray()
        val randomImg = QOIImage(colors, width, height, colorModel, QOIColorSpace.SRGB)

        // when
        writer.write(randomImg, testFileName)
        val readImg = reader.read(testFileName)

        // then
        assertEquals(randomImg, readImg)
    }

    @Test
    fun testWriteReadRandomRGBAImage() {
        // given
        val width = 41
        val height = 23
        val colorModel = QOIColorModel.RGBA
        val colors = Random.nextBytes(width * height * colorModel.channels).map { it.toInt() and 0xFF }.toIntArray()
        val randomImg = QOIImage(colors, width, height, colorModel, QOIColorSpace.SRGB)

        // when
        writer.write(randomImg, testFileName)
        val readImg = reader.read(testFileName)

        // then
        assertEquals(randomImg, readImg)
    }

    @Test
    fun testCreateNegativeSizeFile() {
        assertFailsWith<IllegalArgumentException> {
            // given
            val width = -2
            val height = -2
            val colorModel = QOIColorModel.RGBA
            val colors = intArrayOf()

            // when
            QOIImage(colors, width, height, colorModel, QOIColorSpace.SRGB)
        }
    }

    @Test
    fun testCreateColorsArraySizeDoesntMatchImageSizeFile() {
        assertFailsWith<IllegalArgumentException> {
            // given
            val width = 2
            val height = 2
            val colorModel = QOIColorModel.RGBA
            val colors = intArrayOf(1, 2, 3)

            // when
            QOIImage(colors, width, height, colorModel, QOIColorSpace.SRGB)
        }
    }
}
