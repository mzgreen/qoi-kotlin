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

import okio.BufferedSink
import okio.BufferedSource
import okio.FileHandle
import okio.FileSystem
import okio.Path
import okio.Sink
import okio.Source
import okio.buffer
import okio.use

internal expect val FILE_SYSTEM: FileSystem

private data class Color(val r: Int, val g: Int, val b: Int, val a: Int)

private const val QOI_SRGB = 0
private const val QOI_LINEAR = 1

private const val QOI_OP_INDEX = 0x00 /* 00xxxxxx */
private const val QOI_OP_DIFF = 0x40 /* 01xxxxxx */
private const val QOI_OP_LUMA = 0x80 /* 10xxxxxx */
private const val QOI_OP_RUN = 0xC0 /* 11xxxxxx */
private const val QOI_OP_RGB = 0xFE /* 11111110 */
private const val QOI_OP_RGBA = 0xFF /* 11111111 */

private const val QOI_MASK_2 = 0xC0 /* 11000000 */

private const val QOI_MAGIC = 'q'.code shl 24 or ('o'.code shl 16) or ('i'.code shl 8) or 'f'.code

/* 2GB is the max file size that this implementation can safely handle. We guard
against anything larger than that, assuming the worst case with 5 bytes per
pixel, rounded down to a nice clean value. 400 million pixels ought to be
enough for anybody. */
private const val QOI_PIXELS_MAX = 400_000_000

// Seven 0x00 bytes followed by 0x01
private val QOI_PADDING = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 1)

private fun colorHash(pixel: Color): Int {
    return ((pixel.r and 0xFF) * 3 + (pixel.g and 0xFF) * 5 + (pixel.b and 0xFF) * 7 + (pixel.a and 0xFF) * 11) % 64
}

internal fun decodeQoiImage(
    fileSystem: FileSystem,
    path: Path
): QOIImage = fileSystem.read(path) { decodeQoiImage() }

internal fun decodeQoiImage(fileHandle: FileHandle): QOIImage = decodeQoiImage(fileHandle.source())

internal fun decodeQoiImage(source: Source): QOIImage = source.buffer().use { it.decodeQoiImage() }

private fun BufferedSource.decodeQoiImage(): QOIImage {
    val magic = readInt()
    check(magic == QOI_MAGIC) { "Invalid magic value. Not a QOI file." }

    val width = readInt()
    check(width > 0) { "Width must be a positive value." }

    val height = readInt()
    check(height > 0) { "Height must be a positive value." }

    check(height < QOI_PIXELS_MAX / width) {
        "Image too large. This implementation can handle files up to 2 GB in size."
    }

    val colorModel = when (val b = readByte()) {
        3.toByte() -> QOIColorModel.RGB
        4.toByte() -> QOIColorModel.RGBA
        else -> throw IllegalStateException("Invalid color model value. Was $b expected 3 or 4.")
    }

    val colorSpace = when (val b = readByte()) {
        QOI_SRGB.toByte() -> QOIColorSpace.SRGB
        QOI_LINEAR.toByte() -> QOIColorSpace.LINEAR
        else -> throw IllegalStateException("Invalid color space value. Was $b expected 0 or 1.")
    }

    val index = Array(64) { Color(0, 0, 0, 0) }

    val channels = colorModel.channels
    val pixelCount = width * height * channels
    val colors = IntArray(pixelCount)

    var color = Color(0, 0, 0, 255)

    var run = 0

    for (i in 0 until pixelCount step channels) {
        if (run > 0) {
            run--
        } else {
            val b1 = readByte().toInt() and 0xFF

            if (b1 == QOI_OP_RGB) {
                color = color.copy(
                    r = readByte().toInt() and 0xFF, g = readByte().toInt() and 0xFF, b = readByte().toInt() and 0xFF
                )
            } else if (b1 == QOI_OP_RGBA) {
                color = color.copy(
                    r = readByte().toInt() and 0xFF,
                    g = readByte().toInt() and 0xFF,
                    b = readByte().toInt() and 0xFF,
                    a = readByte().toInt() and 0xFF
                )
            } else if ((b1 and QOI_MASK_2) == QOI_OP_INDEX) {
                color = index[b1]
            } else if ((b1 and QOI_MASK_2) == QOI_OP_DIFF) {
                color = color.copy(
                    r = (color.r + (((b1 shr 4) and 0x03) - 2)) and 0xFF,
                    g = (color.g + (((b1 shr 2) and 0x03) - 2)) and 0xFF,
                    b = (color.b + ((b1 and 0x03) - 2)) and 0xFF
                )
            } else if ((b1 and QOI_MASK_2) == QOI_OP_LUMA) {
                val b2 = readByte().toInt() and 0xFF
                val vg = (b1 and 0x3F) - 32

                color = color.copy(
                    r = (color.r + (vg - 8 + ((b2 shr 4) and 0x0F))) and 0xFF,
                    g = (color.g + (vg and 0xFF)) and 0xFF,
                    b = (color.b + (vg - 8 + (b2 and 0x0F))) and 0xFF,
                )
            } else if ((b1 and QOI_MASK_2) == QOI_OP_RUN) {
                run = b1 and 0x3F
            }

            index[colorHash(color)] = Color(color.r, color.g, color.b, color.a)
        }

        if (channels == 3) {
            colors[i] = color.r
            colors[i + 1] = color.g
            colors[i + 2] = color.b
        } else {
            colors[i] = color.r
            colors[i + 1] = color.g
            colors[i + 2] = color.b
            colors[i + 3] = color.a
        }
    }

    val padding = readByteArray(8)
    check(padding.contentEquals(QOI_PADDING) && exhausted()) { "Invalid padding at the end of the file." }

    return QOIImage(colors, width, height, colorModel, colorSpace)
}

internal fun encodeQoiImage(
    image: QOIImage,
    fileSystem: FileSystem,
    path: Path
) = encodeQoiImage(image, fileSystem.sink(path))

internal fun encodeQoiImage(
    image: QOIImage,
    file: FileHandle
) = encodeQoiImage(image, file.sink())

internal fun encodeQoiImage(
    image: QOIImage,
    sink: Sink
) = sink.buffer().use { it.encodeQoiImage(image) }

private fun BufferedSink.encodeQoiImage(image: QOIImage) {
    check(image.height < QOI_PIXELS_MAX / image.width) {
        "Image too large. This implementation can handle files up to 2 GB in size."
    }

    val channels = image.colorModel.channels
    writeInt(QOI_MAGIC)
    writeInt(image.width)
    writeInt(image.height)
    writeByte(channels)
    writeQOIColorSpace(image.colorSpace)

    val index = Array(64) { Color(0, 0, 0, 0) }

    var prevColor = Color(0, 0, 0, 255)
    var currColor: Color

    val pixelCount = image.width * image.height * channels
    val pixelEnd = pixelCount - channels

    var run = 0
    for (i in image.colors.indices step channels) {
        currColor = image.getColor(i)

        if (currColor == prevColor) {
            run++
            if (run == 62 || i == pixelEnd) {
                writeByte(QOI_OP_RUN or (run - 1))
                run = 0
            }
        } else {
            if (run > 0) {
                writeByte(QOI_OP_RUN or (run - 1))
                run = 0
            }

            val indexPos = colorHash(currColor)

            if (currColor == index[indexPos]) {
                writeByte(QOI_OP_INDEX or indexPos)
            } else {
                index[indexPos] = currColor

                if (currColor.a == prevColor.a) {
                    val vr = (currColor.r - prevColor.r).toByte()
                    val vg = (currColor.g - prevColor.g).toByte()
                    val vb = (currColor.b - prevColor.b).toByte()

                    val vgR = (vr - vg).toByte()
                    val vgB = (vb - vg).toByte()

                    if (vr > -3 && vr < 2 && vg > -3 && vg < 2 && vb > -3 && vb < 2) {
                        writeByte(
                            QOI_OP_DIFF or ((vr + 2) shl 4) and 0xFF or ((vg + 2) shl 2) and 0xFF or (vb + 2) and 0xFF
                        )
                    } else if (vgR > -9 && vgR < 8 && vg > -33 && vg < 32 && vgB > -9 && vgB < 8) {
                        writeByte(QOI_OP_LUMA or (vg + 32) and 0xFF)
                        writeByte(((vgR + 8) shl 4) and 0xFF or (vgB + 8) and 0xFF)
                    } else {
                        writeByte(QOI_OP_RGB)
                        writePixelRGB(currColor)
                    }
                } else {
                    writeByte(QOI_OP_RGBA)
                    writePixelRGBA(currColor)
                }
            }
        }
        prevColor = currColor
    }

    write(QOI_PADDING)
}

private fun QOIImage.getColor(pixelPos: Int): Color = when (colorModel) {
    QOIColorModel.RGB -> {
        Color(
            colors[pixelPos], colors[pixelPos + 1], colors[pixelPos + 2], 255
        )
    }
    QOIColorModel.RGBA -> {
        Color(
            colors[pixelPos], colors[pixelPos + 1], colors[pixelPos + 2], colors[pixelPos + 3]
        )
    }
}

private fun BufferedSink.writeQOIColorSpace(colorSpace: QOIColorSpace) {
    writeByte(
        when (colorSpace) {
            QOIColorSpace.SRGB -> QOI_SRGB
            QOIColorSpace.LINEAR -> QOI_LINEAR
        }
    )
}

private fun BufferedSink.writePixelRGB(color: Color) {
    writeByte(color.r)
    writeByte(color.g)
    writeByte(color.b)
}

private fun BufferedSink.writePixelRGBA(color: Color) {
    writeByte(color.r)
    writeByte(color.g)
    writeByte(color.b)
    writeByte(color.a)
}
