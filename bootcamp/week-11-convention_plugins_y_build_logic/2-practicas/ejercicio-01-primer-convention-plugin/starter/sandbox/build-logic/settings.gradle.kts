// Ya está hecho: build-logic es un build aparte, con sus propios repositorios y el mismo catálogo que el build principal.
// mavenCentral() es para el plugin de Kotlin; `kotlin-dsl` sale del Gradle Plugin Portal, el repositorio de plugins por defecto.
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
