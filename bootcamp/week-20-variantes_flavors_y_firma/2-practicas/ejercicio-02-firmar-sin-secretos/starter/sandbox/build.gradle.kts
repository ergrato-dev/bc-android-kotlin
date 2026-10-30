// Ya está hecho: el raíz no es una app. Declara los plugins con `apply false` para que se carguen una vez, en el
// mismo sitio (semana 10). El de Kotlin tiene además otro motivo: AGP 9 compila Kotlin él solo, con el Kotlin que trae
// (2.2.10). Esta línea pone el plugin de Kotlin 2.4.21 en el classpath del build, y AGP compila con esa versión.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.jvm) apply false
}
