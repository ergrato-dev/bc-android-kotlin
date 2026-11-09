package dev.bitacora

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// Paso 3: lo que crea BitacoraApp, aquí, con @Provides y @Singleton. HiltTest lo cambia por FakeModule.
@Module
@InstallIn(SingletonComponent::class)
object AppModule
