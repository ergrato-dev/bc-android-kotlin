// Ya está hecho: KSP ejecuta el compilador de Dagger, que lee las anotaciones y escribe DaggerTripComponent en
// build/generated/ksp. Si el grafo está mal (falta una dependencia, sobra otra), falla aquí, al compilar.
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

dependencies {
    implementation(libs.dagger)
    ksp(libs.dagger.compiler)
    // El compilador de Dagger trae guava 33.6.0-jre (GHSA-xxph-c9ww-hj94): la corregida.
    constraints {
        ksp(libs.guava)
    }
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.kotlin.test)
    testRuntimeOnly(libs.junit.launcher)
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed")
    }
}
