import java.io.ByteArrayOutputStream
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

group = "com.github.deano2390"
version = "2.1.0"

android {
    namespace = "uk.co.deanwild.materialshowcaseview"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    lint {
        abortOnError = true
        warningsAsErrors = false
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

kotlin {
    compilerOptions {
        // Compile against an older Kotlin API/language level so apps on older Kotlin
        // compilers can still consume the library.
        apiVersion.set(KotlinVersion.KOTLIN_2_2)
        languageVersion.set(KotlinVersion.KOTLIN_2_2)
    }
    coreLibrariesVersion = "2.2.0"
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.activity)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.truth)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = project.group.toString()
            artifactId = "MaterialShowcaseView"
            version = project.version.toString()
            afterEvaluate { from(components["release"]) }
            pom {
                name.set("MaterialShowcaseView")
                description.set("A Material Design themed ShowcaseView for Android")
                url.set("https://github.com/deano2390/MaterialShowcaseView")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
            }
        }
    }
}

/*
 * Public API guard.
 *
 * `./gradlew :library:apiDump` writes the public JVM signatures (as seen by Java callers) to
 * library/api/library.api; `apiCheck` (part of `check`) fails if they change without the dump
 * being updated, so API changes are always deliberate and visible in review.
 */
abstract class ApiSignatureTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val classes: ConfigurableFileCollection

    @get:Input
    abstract val javap: Property<String>

    @get:Inject
    abstract val exec: ExecOperations

    protected fun render(): String {
        val files = classes.asFileTree.matching { include("**/*.class") }.files
            .filterNot { it.name == "R.class" || it.name.startsWith("R$") || it.name == "BuildConfig.class" }
            .sortedBy { it.path }
        if (files.isEmpty()) throw GradleException("No compiled classes found for the API dump")
        val out = ByteArrayOutputStream()
        exec.exec {
            commandLine(listOf(javap.get(), "-public") + files.map { it.absolutePath })
            standardOutput = out
        }
        // One block per class; keep only public/protected classes and drop internal (mangled) members.
        return out.toString().split(Regex("(?m)^Compiled from .*\\n"))
            .map { it.trim() }
            .filter { it.startsWith("public ") || it.startsWith("protected ") }
            .map { block -> block.lines().filterNot { it.contains("\$library") || it.contains("access\$") }.joinToString("\n") }
            .sorted()
            .joinToString("\n\n", postfix = "\n")
    }
}

abstract class ApiDumpTask : ApiSignatureTask() {
    @get:OutputFile
    abstract val dump: RegularFileProperty

    @TaskAction
    fun write() = dump.get().asFile.writeText(render())
}

abstract class ApiCheckTask : ApiSignatureTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val dump: RegularFileProperty

    @TaskAction
    fun check() {
        val expected = dump.get().asFile.readText()
        val actual = render()
        if (expected != actual) {
            val diff = actual.lines().filterNot { it in expected.lines() }.map { "+ $it" } +
                expected.lines().filterNot { it in actual.lines() }.map { "- $it" }
            throw GradleException(
                "Public API changed. If intended, run ./gradlew :library:apiDump and commit library/api.\n" +
                    diff.take(40).joinToString("\n"),
            )
        }
    }
}

val javapPath = javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(21)) }
    .map { it.metadata.installationPath.file("bin/javap").asFile.absolutePath }
val releaseClasses = files(
    layout.buildDirectory.dir("intermediates/built_in_kotlinc/release/compileReleaseKotlin/classes"),
    layout.buildDirectory.dir("intermediates/javac/release/compileReleaseJavaWithJavac/classes"),
).builtBy("compileReleaseKotlin", "compileReleaseJavaWithJavac")
val apiFile = layout.projectDirectory.file("api/library.api")

tasks.register<ApiDumpTask>("apiDump") {
    group = "verification"
    description = "Writes the public API signatures to api/library.api"
    classes.from(releaseClasses)
    javap.set(javapPath)
    dump.set(apiFile)
}

val apiCheck = tasks.register<ApiCheckTask>("apiCheck") {
    group = "verification"
    description = "Fails if the public API differs from api/library.api"
    classes.from(releaseClasses)
    javap.set(javapPath)
    dump.set(apiFile)
}

tasks.named("check") { dependsOn(apiCheck) }
