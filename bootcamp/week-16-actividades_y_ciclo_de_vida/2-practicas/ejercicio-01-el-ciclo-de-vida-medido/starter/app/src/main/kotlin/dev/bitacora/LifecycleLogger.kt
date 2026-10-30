package dev.bitacora

import androidx.lifecycle.DefaultLifecycleObserver

// Paso 3: un observador del ciclo de vida que apunta en `events` "observer.onCreate", "observer.onStart"... por cada
//         método de DefaultLifecycleObserver (onCreate, onStart, onResume, onPause, onStop, onDestroy).
class LifecycleLogger(private val events: MutableList<String>) : DefaultLifecycleObserver
