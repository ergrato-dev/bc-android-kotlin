package dev.bitacora

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

// Ya está hecho: el servidor de las copias. Los tests ponen uno de mentira.
fun interface BackupServer {
    suspend fun upload(file: String): Upload
}

// Ya está hecho: lo que contesta. Busy es un 503 (más tarde puede ir); Rejected, un 4xx (no tiene arreglo).
sealed interface Upload {
    data class Stored(val bytes: Long) : Upload
    data object Busy : Upload
    data object Rejected : Upload
}

const val KEY_FILE = "file"
const val KEY_BYTES = "bytes"
const val MAX_TRIES = 3
const val BACKUP_TAG = "backup"

// La copia de un cuaderno. El nombre del archivo llega en los datos de entrada; el tamaño guardado sale en los de
// salida.
class BackupWorker(context: Context, params: WorkerParameters, private val server: BackupServer) :
    CoroutineWorker(context, params) {
    // Paso 2: sin archivo, failure(); si no, server.upload(file), y un Result por respuesta. Ojo con runAttemptCount.
    override suspend fun doWork(): Result = Result.success()
}

// Ya está hecho: WorkManager crea los workers por su nombre; esta fábrica le da a BackupWorker su servidor.
class BackupFactory(private val server: BackupServer) : WorkerFactory() {
    override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? =
        if (workerClassName == BackupWorker::class.java.name) BackupWorker(appContext, workerParameters, server) else null
}

// Paso 3: con wifi (sin gastar datos) y sin la batería baja. Si el servidor está ocupado: a los 20 s, a los 40 s...
fun backupRequest(file: String): OneTimeWorkRequest =
    OneTimeWorkRequestBuilder<BackupWorker>()
        .setInputData(workDataOf(KEY_FILE to file))
        .addTag(BACKUP_TAG)
        .build()

// Paso 4: una copia por cuaderno; pedirla otra vez mientras espera no hace otra.
fun WorkManager.backUp(file: String) {
    enqueue(backupRequest(file))
}

// Paso 5: y una cada día, solo con wifi y cargando: de noche, en la mesilla.
fun WorkManager.backUpEveryDay(file: String) {
    val request = PeriodicWorkRequestBuilder<BackupWorker>(15, TimeUnit.MINUTES)
        .setInputData(workDataOf(KEY_FILE to file))
        .addTag(BACKUP_TAG)
        .build()
    enqueue(request)
}
