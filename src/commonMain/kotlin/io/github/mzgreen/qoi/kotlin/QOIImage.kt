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

/**
 * A class representing an immutable [QOI](https://qoiformat.org/) image with
 * each pixel value set to the corresponding value in the colors array and with
 * the specified width, height, color model and color space.
 *
 * This implementation can safely handle files up to 2 GB in size.
 *
 *
 * @property colors An array of raw pixel colors stored as RGB or RGBA.
 * This array size must be equal to width * height * [QOIColorModel.channels].
 * @property width The width of the image.
 * @property height The height of the image.
 * @property colorModel The color model of the image.
 * @property colorSpace The color space of the image.
 * @throws IllegalArgumentException if provided data is not a valid QOI image.
 */
public class QOIImage(
    public val colors: IntArray,
    public val width: Int,
    public val height: Int,
    public val colorModel: QOIColorModel,
    public val colorSpace: QOIColorSpace
) {
    init {
        require(width > 0) { "Width must be a positive value." }
        require(height > 0) { "Height must be a positive value." }
        require(colors.size == width * height * colorModel.channels) {
            "The size of the colors array must be equal to with * height * colorModel.channels."
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as QOIImage

        if (!colors.contentEquals(other.colors)) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (colorModel != other.colorModel) return false
        if (colorSpace != other.colorSpace) return false

        return true
    }

    override fun hashCode(): Int {
        var result = colors.contentHashCode()
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + colorModel.hashCode()
        result = 31 * result + colorSpace.hashCode()
        return result
    }
}
