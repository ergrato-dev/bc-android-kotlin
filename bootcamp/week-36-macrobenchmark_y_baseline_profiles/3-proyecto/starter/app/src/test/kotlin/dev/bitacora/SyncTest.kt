package dev.bitacora

import dev.bitacora.data.testing.FakeRepository
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.BackoffPolicy
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import dev.bitacora.model.Level
import dev.bitacora.model.Observation
import dev.bitacora.network.NetworkError
import dev.bitacora.data.RefreshResult
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

// Semana 32: el worker y la cola de WorkManager, con el FakeRepository. Con SynchronousExecutor, WorkManager hace todo
// en el hilo del test; con el TestDriver, el test decide cuándo hay red.
@RunWith(AndroidJUnit4::class)
class SyncTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repository = FakeRepository()
    private val factory = SyncWorkerFactory { repository }

    private fun work(attempt: Int = 0): ListenableWorker.Result = runBlocking {
        TestListenableWorkerBuilder<SyncWorker>(context).setWorkerFactory(factory).setRunAttemptCount(attempt).build().doWork()
    }

    @Test
    fun whenItArrivesItIsASuccess() {
        assertEquals(ListenableWorker.Result.success(), work())
        assertEquals(1, repository.refreshes)
    }

    @Test
    fun withoutNetworkItTriesAgain() {
        repository.answer.value = RefreshResult.Failed(NetworkError.OFFLINE)
        assertEquals(ListenableWorker.Result.retry(), work())
    }

    // Repetir no arregla una respuesta que no se entiende.
    @Test
    fun aBadResponseIsAFailure() {
        repository.answer.value = RefreshResult.Failed(NetworkError.BAD_RESPONSE)
        assertEquals(ListenableWorker.Result.failure(), work())
    }

    // runAttemptCount empieza en 0: el quinto intento es el 4, y es el último.
    @Test
    fun afterFiveAttemptsItGivesUp() {
        repository.answer.value = RefreshResult.Failed(NetworkError.TIMEOUT)
        assertEquals(ListenableWorker.Result.retry(), work(attempt = 3))
        assertEquals(ListenableWorker.Result.failure(), work(attempt = 4))
    }

    private lateinit var workManager: WorkManager

    @Before
    fun testWorkManager() {
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder().setExecutor(SynchronousExecutor()).setWorkerFactory(factory).build(),
        )
        workManager = WorkManager.getInstance(context)
    }

    private fun sync() = workManager.getWorkInfosForUniqueWork(SYNC_WORK).get()

    private fun networkArrives() = WorkManagerTestInitHelper.getTestDriver(context)!!.setAllConstraintsMet(sync().single().id)

    // Guardar no espera a la red: el cambio está, y la sincronización, en cola hasta que haya red.
    @Test
    fun savingQueuesASyncThatWaitsForTheNetwork() = runBlocking {
        SyncingRepository(repository, workManager).save(listOf(Observation("A-1", Level.HIGH, "roya")))
        val info = sync().single()
        assertEquals(WorkInfo.State.ENQUEUED, info.state)
        assertEquals(NetworkType.CONNECTED, info.constraints.requiredNetworkType)
        assertEquals(0, repository.refreshes)
        networkArrives()
        assertEquals(WorkInfo.State.SUCCEEDED, sync().single().state)
        assertEquals(1, repository.refreshes)
    }

    // Tres cambios sin red: una sola sincronización en cola (REPLACE), no tres.
    @Test
    fun severalChangesOneSync() = runBlocking {
        val syncing = SyncingRepository(repository, workManager)
        repeat(3) { syncing.save(listOf(Observation("A-${it + 1}", Level.LOW, "riego"))) }
        assertEquals(listOf(WorkInfo.State.ENQUEUED), sync().map { it.state })
        networkArrives()
        assertEquals(1, repository.refreshes)
    }

    // Sin red otra vez: vuelve a la cola, con un intento más y la espera exponencial de 30 s.
    @Test
    fun aFailedSyncWaitsAndTriesAgain() = runBlocking {
        repository.answer.value = RefreshResult.Failed(NetworkError.OFFLINE)
        SyncingRepository(repository, workManager).save(emptyList())
        networkArrives()
        val info = sync().single()
        assertEquals(WorkInfo.State.ENQUEUED, info.state)
        assertEquals(1, info.runAttemptCount)
        assertEquals(BackoffPolicy.EXPONENTIAL to 30_000L, workManager.getWorkInfoById(info.id).get()!!.let {
            BackoffPolicy.EXPONENTIAL to (it.nextScheduleTimeMillis - System.currentTimeMillis() + 500) / 1_000 * 1_000
        })
    }

    // WorkManager pide su configuración a la aplicación (Configuration.Provider), con la fábrica de SyncWorker. Sin
    // ella, crearía SyncWorker por reflexión, con dos parámetros, y el trabajo acabaría en FAILED.
    @Test
    fun theAppGivesWorkManagerTheFactory() {
        assertIs<SyncWorkerFactory>((context as Configuration.Provider).workManagerConfiguration.workerFactory)
    }

    // Y el inicializador automático no está: arrancaría WorkManager con la configuración de serie, sin la fábrica. El
    // proveedor de androidx.startup se queda: lo usan otras bibliotecas.
    @Test
    fun theAutomaticInitializerIsRemoved() {
        val startup = context.packageManager.getProviderInfo(
            ComponentName(context, "androidx.startup.InitializationProvider"),
            PackageManager.GET_META_DATA,
        )
        assertTrue("androidx.work.WorkManagerInitializer" !in startup.metaData.keySet(), startup.metaData.keySet().toString())
    }
}
