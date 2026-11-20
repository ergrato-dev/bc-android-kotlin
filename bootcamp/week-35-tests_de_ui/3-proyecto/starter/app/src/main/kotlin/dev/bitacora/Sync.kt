package dev.bitacora

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dev.bitacora.model.Observation
import dev.bitacora.network.NetworkError
import dev.bitacora.data.ObservationRepository
import dev.bitacora.data.RefreshResult
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Provider

const val SYNC_WORK = "sync"
const val MAX_ATTEMPTS = 5

// La sincronización: refresh baja, decide los conflictos y sube la cola. Si falla la red, otra vez más tarde; si el
// servidor manda algo que no se entiende, repetirlo no lo arregla. Tras MAX_ATTEMPTS intentos se rinde: la cola sigue
// en Room, y el próximo cambio vuelve a intentarlo.
class SyncWorker(context: Context, params: WorkerParameters, private val repository: ObservationRepository) :
    CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = when (val result = repository.refresh()) {
        is RefreshResult.Done -> Result.success()
        is RefreshResult.Failed ->
            if (result.error == NetworkError.BAD_RESPONSE || runAttemptCount + 1 >= MAX_ATTEMPTS) Result.failure() else Result.retry()
    }
}

// WorkManager crea los workers por su nombre, con un constructor de (Context, WorkerParameters). SyncWorker necesita
// además el repositorio: esta fábrica se lo da. Provider: el repositorio se pide al crear el worker, no al crear la
// fábrica (la fábrica la necesita BitacoraApp antes de que exista nada más).
class SyncWorkerFactory @Inject constructor(private val repository: Provider<ObservationRepository>) : WorkerFactory() {
    override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? =
        if (workerClassName == SyncWorker::class.java.name) SyncWorker(appContext, workerParameters, repository.get()) else null
}

// Una sola sincronización a la vez, con red. REPLACE: un cambio mientras una espera (o corre) la sustituye; la nueva
// lee la cola entera, con el cambio. El backoff exponencial de 30 s es el de serie: escrito, para que se vea.
fun WorkManager.sync() {
    val request = OneTimeWorkRequestBuilder<SyncWorker>()
        .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
        .build()
    enqueueUniqueWork(SYNC_WORK, ExistingWorkPolicy.REPLACE, request)
}

// El repositorio de siempre, y después de cada cambio, una sincronización en cola. El ViewModel no sabe nada de esto.
class SyncingRepository(private val repository: ObservationRepository, private val workManager: WorkManager) :
    ObservationRepository by repository {
    override suspend fun save(observations: List<Observation>) {
        repository.save(observations)
        workManager.sync()
    }
}
