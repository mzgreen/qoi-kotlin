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

import okio.FileHandle
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import okio.Sink

/**
 * A class for writing [QOIImage].
 */
public class QOIWriter {

    private val fileSystem: FileSystem

    /**
     * Creates a new [QOIReader].
     */
    public constructor() : this(FILE_SYSTEM)

    /**
     * Creates a new [QOIReader].
     *
     * @property fileSystem An instance of [FileSystem] that will be used for reading files.
     */
    public constructor(fileSystem: FileSystem) {
        this.fileSystem = fileSystem
    }

    /**
     * Writes the [QOIImage] into a file represented as a String path.
     *
     * @param image An instance of [QOIImage].
     * @param path A string representing a path to a file in which the image will be saved.
     * @throws IllegalStateException If image is too large (more than 2 GB).
     */
    @Throws(IOException::class, IllegalStateException::class)
    public fun write(image: QOIImage, path: String) {
        encodeQoiImage(image, fileSystem, path.toPath())
    }

    /**
     * Writes the [QOIImage] into a file represented as a [Path] object.
     *
     * @param image An instance of [QOIImage].
     * @param path A [Path] object representing a file in which the image will be saved.
     * @throws IllegalStateException If image is too large (more than 2 GB).
     */
    @Throws(IOException::class, IllegalStateException::class)
    public fun write(image: QOIImage, path: Path) {
        encodeQoiImage(image, fileSystem, path)
    }

    /**
     * Writes the [QOIImage] into a file represented as a [FileHandle] object.
     * This method doesn't close the provided [FileHandle]. It's up to the caller to close it.
     *
     * @param image An instance of [QOIImage].
     * @param file A [FileHandle] in which the image will be saved.
     * @throws IllegalStateException If image is too large (more than 2 GB).
     */
    @Throws(IOException::class, IllegalStateException::class)
    public fun write(image: QOIImage, file: FileHandle) {
        encodeQoiImage(image, file)
    }

    /**
     * Writes the [QOIImage] object into a [Sink].
     *
     * @param image An instance of [QOIImage].
     * @param sink A [Sink] in which the image will be written.
     * @throws IllegalStateException if image is too large (more than 2 GB).
     */
    @Throws(IOException::class, IllegalStateException::class)
    public fun write(image: QOIImage, sink: Sink) {
        encodeQoiImage(image, sink)
    }
}
