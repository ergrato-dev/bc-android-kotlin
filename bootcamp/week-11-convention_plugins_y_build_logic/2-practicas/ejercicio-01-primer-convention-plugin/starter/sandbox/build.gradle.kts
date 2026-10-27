// Ya está hecho (semana 10): los plugins con su versión, una vez, sin aplicarlos.
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
