// Ya está hecho: el proyecto raíz solo declara los plugins con su versión, una vez, y no los aplica (apply false).
// Los módulos los aplican sin versión: plugins { kotlin("jvm") }.
plugins {
    kotlin("jvm") version "2.4.21" apply false
    kotlin("plugin.serialization") version "2.4.21" apply false
}
