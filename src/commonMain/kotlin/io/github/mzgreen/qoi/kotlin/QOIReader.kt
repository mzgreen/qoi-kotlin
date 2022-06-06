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
import okio.Source

/**
 * A class for reading [QOIImage].
 */
public class QOIReader {

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
     * Reads the [QOIImage] from a path represented as a String.
     *
     * @param path A string representing a path to a file from which the image will be decoded.
     * @return An immutable instance of [QOIImage].
     * @throws IllegalStateException If provided data is not a valid QOI image.
     */
    @Throws(IOException::class, IllegalStateException::class)
    public fun read(path: String): QOIImage {
        return decodeQoiImage(fileSystem, path.toPath())
    }

    /**
     * Reads the [QOIImage] from a file represented as a [Path] object.
     *
     * @param path A [Path] object representing a file from which the image will be decoded.
     * @return An immutable instance of [QOIImage].
     * @throws IllegalStateException If provided data is not a valid QOI image.
     */
    @Throws(IOException::class, IllegalStateException::class)
    public fun read(path: Path): QOIImage {
        return decodeQoiImage(fileSystem, path)
    }

    /**
     * Reads the [QOIImage] from a file represented as a [FileHandle].
     *
     * @param file A [FileHandle] from which the image will be decoded.
     * @return An immutable instance of [QOIImage].
     * @throws IllegalStateException If provided data is not a valid QOI image.
     */
    @Throws(IOException::class, IllegalStateException::class)
    public fun read(file: FileHandle): QOIImage {
        return decodeQoiImage(file)
    }

    /**
     * Reads the [QOIImage] from a [Source].
     *
     * @param source A [Source] from which the image will be decoded.
     * @return An immutable instance of [QOIImage].
     * @throws IllegalStateException If provided data is not a valid QOI image.
     */
    @Throws(IOException::class, IllegalStateException::class)
    public fun read(source: Source): QOIImage {
        return decodeQoiImage(source)
    }
}
