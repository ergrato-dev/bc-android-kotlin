// Paso 2: los plugins con su versión, una vez, sin aplicarlos. Cuando tengas el catálogo, cámbialos a
//         alias(libs.plugins.kotlin.jvm) apply false (y el de serialization).
plugins {
    kotlin("jvm") version "2.4.21" apply false
    kotlin("plugin.serialization") version "2.4.21" apply false
}
