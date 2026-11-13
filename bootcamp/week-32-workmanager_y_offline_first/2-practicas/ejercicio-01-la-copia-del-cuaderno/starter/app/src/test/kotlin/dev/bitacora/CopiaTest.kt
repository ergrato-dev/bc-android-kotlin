package dev.bitacora

import android.content.Context
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
import androidx.work.workDataOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class CopiaTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    // El servidor de mentira: contesta lo que haya en answers (y si no queda nada, Stored(2048)), y apunta las llamadas.
    private val answers = ArrayDeque<Upload>()
    private val calls = mutableListOf<String>()
    private val factory = BackupFactory { file ->
        calls += file
        answers.removeFirstOrNull() ?: Upload.Stored(2048)
    }

    private fun work(file: String? = "campo.db", attempt: Int = 0): ListenableWorker.Result = runBlocking {
        TestListenableWorkerBuilder<BackupWorker>(context)
            .setWorkerFactory(factory)
            .setInputData(if (file == null) workDataOf() else workDataOf(KEY_FILE to file))
            .setRunAttemptCount(attempt)
            .build()
            .doWork()
    }

    // El worker, solo: TestListenableWorkerBuilder lo crea y doWork() se llama como una función.

    @Test
    fun aStoredCopyIsASuccessWithItsSize() {
        assertEquals(ListenableWorker.Result.success(workDataOf(KEY_BYTES to 2048L)), work())
        assertEquals(listOf("campo.db"), calls)
    }

    @Test
    fun withoutAFileItFails() {
        assertEquals(ListenableWorker.Result.failure(), work(file = null))
        assertEquals(emptyList(), calls)
    }

    @Test
    fun aBusyServerIsTriedAgain() {
        answers += Upload.Busy
        assertEquals(ListenableWorker.Result.retry(), work())
    }

    @Test
    fun theThirdBusyGivesUp() {
        answers += listOf(Upload.Busy, Upload.Busy)
        assertEquals(ListenableWorker.Result.retry(), work(attempt = 1))
        assertEquals(ListenableWorker.Result.failure(), work(attempt = 2))
    }

    @Test
    fun aRejectedCopyIsNotTriedAgain() {
        answers += Upload.Rejected
        assertEquals(ListenableWorker.Result.failure(), work())
    }

    @Test
    fun theRequestWaitsForWifiAndBattery() {
        val spec = backupRequest("campo.db").workSpec
        assertEquals(NetworkType.UNMETERED, spec.constraints.requiredNetworkType)
        assertTrue(spec.constraints.requiresBatteryNotLow())
        assertEquals(BackoffPolicy.LINEAR, spec.backoffPolicy)
        assertEquals(20_000, spec.backoffDelayDuration)
        assertEquals("campo.db", spec.input.getString(KEY_FILE))
    }

    // Con WorkManager: el de los tests. SynchronousExecutor lo hace todo en el hilo del test, y el TestDriver dice
    // cuándo se cumplen las condiciones.

    private lateinit var workManager: WorkManager

    @Before
    fun testWorkManager() {
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder().setExecutor(SynchronousExecutor()).setWorkerFactory(factory).build(),
        )
        workManager = WorkManager.getInstance(context)
    }

    private fun copies() = workManager.getWorkInfosByTag(BACKUP_TAG).get()

    private fun conditionsMet(info: WorkInfo) = WorkManagerTestInitHelper.getTestDriver(context)!!.setAllConstraintsMet(info.id)

    @Test
    fun withoutWifiItWaits() {
        workManager.backUp("campo.db")
        assertEquals(WorkInfo.State.ENQUEUED, copies().single().state)
        assertEquals(emptyList(), calls)
        conditionsMet(copies().single())
        assertEquals(WorkInfo.State.SUCCEEDED, copies().single().state)
        assertEquals(2048L, copies().single().outputData.getLong(KEY_BYTES, 0))
    }

    @Test
    fun askingTwiceMakesOneCopy() {
        workManager.backUp("campo.db")
        val first = copies().single().id
        workManager.backUp("campo.db")
        assertEquals(listOf(first), copies().map { it.id })
    }

    @Test
    fun twoNotebooksTwoCopies() {
        workManager.backUp("campo.db")
        workManager.backUp("finca.db")
        assertEquals(2, copies().size)
    }

    // Ocupado: vuelve a la cola con un intento más, y a la siguiente, se guarda.
    @Test
    fun aBusyServerWaitsAndTriesAgain() {
        answers += Upload.Busy
        workManager.backUp("campo.db")
        conditionsMet(copies().single())
        assertEquals(WorkInfo.State.ENQUEUED, copies().single().state)
        assertEquals(1, copies().single().runAttemptCount)
        conditionsMet(copies().single())
        assertEquals(WorkInfo.State.SUCCEEDED, copies().single().state)
        assertEquals(2, calls.size)
    }

    @Test
    fun everyDayIsEvery24HoursWhileCharging() {
        workManager.backUpEveryDay("campo.db")
        workManager.backUpEveryDay("campo.db")
        val daily = copies().single()
        assertEquals(TimeUnit.HOURS.toMillis(24), daily.periodicityInfo!!.repeatIntervalMillis)
        assertTrue(daily.constraints.requiresCharging())
        assertEquals(NetworkType.UNMETERED, daily.constraints.requiredNetworkType)
    }
}
