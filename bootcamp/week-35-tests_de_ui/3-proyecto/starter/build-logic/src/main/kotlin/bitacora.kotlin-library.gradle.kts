plugins {
    id("bitacora.kotlin-base")
    `java-library`
    // Semana 34: la cobertura de cada módulo de Kotlin, que el raíz junta en un informe.
    id("org.jetbrains.kotlinx.kover")
}

val libs = versionCatalogs.named("libs")

dependencies {
    testImplementation(platform(libs.findLibrary("junit-bom").orElseThrow { GradleException("El catálogo no tiene la biblioteca junit-bom") }))
    testImplementation(libs.findBundle("testing").orElseThrow { GradleException("El catálogo no tiene el bundle testing") })
}

// Los fakes de testFixtures son código de test, no de la app: si contaran, un fake usado por todos subiría la
// cobertura sin probar nada de verdad.
kover {
    currentProject {
        sources {
            excludedSourceSets.add("testFixtures")
        }
    }
}
