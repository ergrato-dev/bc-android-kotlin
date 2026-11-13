package dev.bitacora

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dev.bitacora.data.ObservationRepository
import javax.inject.Inject
import javax.inject.Provider

const val SYNC_WORK = "sync"
const val MAX_ATTEMPTS = 5

// La sincronización: refresh baja, decide los conflictos y sube la cola.
// Paso 6: Done es success; BAD_RESPONSE, failure (repetir no lo arregla); lo demás, retry, hasta MAX_ATTEMPTS
// intentos (runAttemptCount empieza en 0).
class SyncWorker(context: Context, params: WorkerParameters, private val repository: ObservationRepository) :
    CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = Result.success()
}

// WorkManager crea los workers por su nombre, con un constructor de (Context, WorkerParameters). SyncWorker necesita
// además el repositorio. Provider: el repositorio se pide al crear el worker, no al crear la fábrica.
// Paso 6: si el nombre es el de SyncWorker, uno con repository.get(); si no, null.
class SyncWorkerFactory @Inject constructor(private val repository: Provider<ObservationRepository>) : WorkerFactory() {
    override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? =
        null
}

// Paso 7: una sola sincronización a la vez (SYNC_WORK), con red, backoff exponencial de 30 s.
fun WorkManager.sync() {
}

// Paso 7: el repositorio de siempre, y después de cada save, una sincronización en cola.
class SyncingRepository(private val repository: ObservationRepository, private val workManager: WorkManager) :
    ObservationRepository by repository
