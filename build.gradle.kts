import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.net.URL

plugins {
    kotlin("multiplatform") version "1.6.21"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("org.jetbrains.dokka") version "1.6.21"
    id("maven-publish")
    id("signing")
    id("com.diffplug.spotless") version "6.6.1"
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.10.0"
}

configure<SpotlessExtension> {
    kotlin {
        target("**/*.kt")
        ktlint()
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

val GROUP: String by project
val VERSION_NAME: String by project

group = GROUP
version = VERSION_NAME

repositories {
    mavenCentral()
}

val appleTargets = listOf(
    "iosArm64",
    "iosX64",
    "iosSimulatorArm64",
    "macosX64",
    "macosArm64",
    "tvosArm64",
    "tvosX64",
    "tvosSimulatorArm64",
    "watchosArm32",
    "watchosArm64",
    "watchosX86",
    "watchosX64",
    "watchosSimulatorArm64"
)

val mingwTargets = listOf(
    "mingwX64"
)

val linuxTargets = listOf(
    "linuxX64"
)

val nativeTargets = appleTargets + linuxTargets + mingwTargets

/**
 * Creates a source set for a directory that isn't already a built-in platform. Use this to create
 * custom shared directories like `nonJvmMain` or `unixMain`.
 */
fun NamedDomainObjectContainer<KotlinSourceSet>.createSourceSet(
    name: String,
    parent: KotlinSourceSet? = null,
    children: List<String> = listOf()
): KotlinSourceSet {
    val result = create(name)

    if (parent != null) {
        result.dependsOn(parent)
    }

    val suffix = when {
        name.endsWith("Main") -> "Main"
        name.endsWith("Test") -> "Test"
        else -> error("unexpected source set name: $name")
    }

    for (childTarget in children) {
        val childSourceSet = get("${childTarget}$suffix")
        childSourceSet.dependsOn(result)
    }

    return result
}

kotlin {
    explicitApi()
    jvm()
    js(BOTH) {
        compilations.all {
            kotlinOptions {
                sourceMap = true
                moduleKind = "umd"
                metaInfo = true
            }
        }
        nodejs()
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()
    watchosArm32()
    watchosArm64()
    watchosX86()
    watchosX64()
    watchosSimulatorArm64()
    // Required to generate tests tasks: https://youtrack.jetbrains.com/issue/KT-26547
    linuxX64()
    macosX64()
    macosArm64()
    mingwX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("com.squareup.okio:okio:3.1.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("com.squareup.okio:okio-fakefilesystem:3.1.0")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("com.squareup.okio:okio-nodefilesystem:3.1.0")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        createSourceSet("nativeMain", parent = commonMain, children = nativeTargets)
    }
}

tasks {
    val dokkaHtml by getting(DokkaTask::class) {
        dokkaSourceSets {
            configureEach {
                reportUndocumented.set(false)
                skipEmptyPackages.set(true)
                skipDeprecated.set(true)
                jdkVersion.set(8)

                sourceLink {
                    localDirectory.set(project.file("src/commonMain/kotlin"))
                    // URL showing where the source code can be accessed through the web browser
                    remoteUrl.set(URL("https://github.com/mzgreen/qoi-kotlin/blob/main/${project.name}/src/commonMain/kotlin"))
                    // Suffix which is used to append the line number to the URL. Use #L for GitHub
                    remoteLineSuffix.set("#L")
                }
            }
        }
    }
}

val dokkaHtml by tasks.getting(DokkaTask::class)

val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaHtml.outputDirectory)
}

apply(from = "publish.gradle")
