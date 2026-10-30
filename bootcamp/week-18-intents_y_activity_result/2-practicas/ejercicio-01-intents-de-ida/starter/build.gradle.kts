// Ya está hecho: AGP y los plugins de Kotlin se cargan una vez, en el raíz (semana 15, teoría 03).
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
}
