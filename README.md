<img src="https://qoiformat.org/qoi-logo.svg" alt="QoiSharp" width="256"/>

# qoi-kotlin

[![qoi-kotlin](https://maven-badges.herokuapp.com/maven-central/io.github.mzgreen/qoi-kotlin/badge.svg?subject=qoi-kotlin)](https://maven-badges.herokuapp.com/maven-central/io.github.mzgreen/qoi-kotlin)

A Kotlin Multiplatform implementation of [Quite OK Image Format](https://qoiformat.org/).

This library depends on [Okio](https://github.com/square/okio) which provides multiplatform file system APIs.

## Maven

```xml

<dependency>
    <groupId>io.github.mzgreen</groupId>
    <artifactId>qoi-kotlin</artifactId>
    <version>1.0.1</version>
</dependency>
```

## Gradle

```gradle
repositories {
    // ...
    mavenCentral()
}

dependencies {
    implementation 'io.github.mzgreen:qoi-kotlin:1.0.1'
}
```

## Usage

#### Reading QOI images

Use `QOIReader` to read QOI images from disk:

```kotlin
// Create reader
val qoiReader = QOIReader()

// Read from String path
val qoiImage: QOIImage = qoiReader.read("image.qoi")

// Read from Okio's Path
val path: Path = // ...
val qoiImage: QOIImage = qoiReader.read(path)

// Read from Okio's FileHandle
val fileHandle: FileHandle = // ...
val qoiImage: QOIImage = qoiReader.read(fileHandle)

// Read from Okio's Source
val source: Source = // ...
val qoiImage: QOIImage = qoiReader.read(source)
```

#### Writing QOI images

Use `QOIWriter` to write QOI images to disk:

```kotlin
val qoiImage: QOIImage = // ...

// Create writer
val qoiWriter = QOIWriter()

// Write to String path
qoiWriter.write(qoiImage, "image.qoi")

// Write to Okio's Path
val path: Path = // ...
qoiWriter.write(qoiImage, path)

// Write to Okio's FileHandle
val fileHandle: FileHandle = // ... 
qoiWriter.write(qoiImage, fileHandle)

// Write to Okio's Sink
val sink: Sink = // ... 
qoiWriter.write(qoiImage, sink)
```

## Building the project

```bash
$ ./gradlew build
```

## License

MIT License

```
Copyright (c) 2022 mzgreen

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
