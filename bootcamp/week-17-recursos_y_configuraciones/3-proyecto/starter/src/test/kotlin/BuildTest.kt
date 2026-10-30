import kotlin.test.Test
import kotlin.test.assertEquals

class BuildTest {
    private fun debugApk(project: Project = Project()): Apk {
        project.run(":app:assembleDebug")
        return Apk(project.file("app/build/outputs/apk/debug/app-debug.apk"))
    }

    // Ejemplo: solo tus idiomas en el APK. "default" es tu idioma por defecto (values/), "en" tu traducción y "es" los
    // textos en español de AndroidX, que el filtro deja pasar. Cámbialo a los idiomas de tu dominio.
    @Test
    fun onlyTheAppLanguagesShip() {
        assertEquals(listOf("default", "en", "es"), debugApk().analyze("resources", "configs", "--type", "string").lines())
    }
}
