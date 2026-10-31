plugins {
    id("bitacora.kotlin-base")
    `java-library`
}

val libs = versionCatalogs.named("libs")

dependencies {
    testImplementation(platform(libs.findLibrary("junit-bom").orElseThrow { GradleException("El catálogo no tiene la biblioteca junit-bom") }))
    testImplementation(libs.findBundle("testing").orElseThrow { GradleException("El catálogo no tiene el bundle testing") })
}
