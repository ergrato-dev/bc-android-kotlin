package dev.bitacora

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// Hilt genera aquí el grafo de la app (SingletonComponent): vive lo que vive la aplicación. Lo que creaba a mano está
// ahora en AppModule.
@HiltAndroidApp
class BitacoraApp : Application()
