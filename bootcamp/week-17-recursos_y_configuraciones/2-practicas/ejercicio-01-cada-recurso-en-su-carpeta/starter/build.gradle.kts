// Ya está hecho: AGP y Kotlin se cargan una vez, en el raíz (semana 15, teoría 03).
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.jvm) apply false
}
