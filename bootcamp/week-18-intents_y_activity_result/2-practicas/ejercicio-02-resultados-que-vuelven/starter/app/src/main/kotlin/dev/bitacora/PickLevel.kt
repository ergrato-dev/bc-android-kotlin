package dev.bitacora

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

enum class Level { LOW, MEDIUM, HIGH }

const val EXTRA_LEVEL = "dev.bitacora.extra.LEVEL"

// Paso 1: el contrato. La entrada es el nivel actual y la salida, el elegido (null si el usuario no eligió ninguno).
class PickLevel : ActivityResultContract<Level, Level?>() {
    // Un Intent explícito a LevelPickerActivity con el nivel actual (su name) en EXTRA_LEVEL.
    override fun createIntent(context: Context, input: Level): Intent = TODO("Paso 1")

    // El nivel de EXTRA_LEVEL solo si resultCode es RESULT_OK; si no, null.
    override fun parseResult(resultCode: Int, intent: Intent?): Level? = TODO("Paso 1")
}
