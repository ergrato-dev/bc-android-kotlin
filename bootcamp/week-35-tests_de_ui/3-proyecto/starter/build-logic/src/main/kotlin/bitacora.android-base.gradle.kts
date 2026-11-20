import com.android.build.api.dsl.CommonExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

// Lo común de la app y de las bibliotecas de Android de la Bitácora (semana 33). CommonExtension es lo que tienen las
// dos extensiones, ApplicationExtension y LibraryExtension: el SDK, el minSdk y los tests en Robolectric. Este plugin
// no aplica el de Android: lo aplica quien lo usa, bitacora.android-application o bitacora.android-library.
extensions.configure<CommonExtension>("android") {
    compileSdk = 37
    defaultConfig.minSdk = 26
    // En CommonExtension, testOptions es una propiedad, no un bloque.
    testOptions.unitTests.isIncludeAndroidResources = true
    testOptions.unitTests.all { it.jvmArgs("--add-opens=java.base/jdk.internal.access=ALL-UNNAMED") }
}

// AGP 9 trae Kotlin: su extensión se llama kotlin, pero sin el plugin en plugins { } no hay accesor.
extensions.configure<KotlinAndroidProjectExtension>("kotlin") {
    jvmToolchain(21)
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

val libs = versionCatalogs.named("libs")
fun library(name: String) = libs.findLibrary(name).orElseThrow { GradleException("El catálogo no tiene la biblioteca $name") }

val androidAll = configurations.dependencyScope("androidAll")
val androidAllJars = configurations.resolvable("androidAllJars") { extendsFrom(androidAll.get()) }

dependencies {
    "testImplementation"(libs.findBundle("robolectric").orElseThrow { GradleException("El catálogo no tiene el bundle robolectric") })
    "androidAll"(library("android-all"))
    constraints {
        "testImplementation"(library("guava")) {
            because("GHSA-xxph-c9ww-hj94: Robolectric 4.17 trae guava 33.6.0-jre, afectada; corregida en 33.7.2")
        }
    }
}

tasks.withType<Test>().configureEach {
    val jars = files(androidAllJars)
    inputs.files(jars).withNormalizer(ClasspathNormalizer::class.java).withPropertyName("androidAll")
    systemProperty("robolectric.offline", "true")
    jvmArgumentProviders += CommandLineArgumentProvider { listOf("-Drobolectric.dependency.dir=${jars.singleFile.parentFile}") }
}
