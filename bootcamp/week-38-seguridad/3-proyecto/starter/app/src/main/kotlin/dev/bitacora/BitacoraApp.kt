package dev.bitacora

import android.app.Application
import android.os.StrictMode
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

// Hilt genera aquí el grafo de la app (SingletonComponent): vive lo que vive la aplicación. Lo que creaba a mano está
// ahora en AppModule.
// Semana 32: también la configuración de WorkManager, con la fábrica que sabe crear SyncWorker. WorkManager la pide
// la primera vez que alguien llama a WorkManager.getInstance (el inicializador automático está quitado en el
// manifiesto: no sabría de la fábrica).
@HiltAndroidApp
class BitacoraApp : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: SyncWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build())
        }
    }
}
