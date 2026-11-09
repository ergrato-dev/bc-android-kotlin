package dev.bitacora

import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import java.io.File

// Qué implementación recibe quien pide la interfaz.
@Module
interface TripModule {
    // Paso 1: la del archivo.
    @Binds
    fun repository(memory: InMemoryChecklistRepository): ChecklistRepository
}

// El grafo: lo que se le puede pedir, y lo que hay que darle al crearlo (la carpeta).
// Paso 2: una sola lista para todo el componente.
@Component(modules = [TripModule::class])
interface TripComponent {
    fun planner(): Planner

    fun repository(): ChecklistRepository

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance dir: File): TripComponent
    }
}
