package dev.bitacora

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

// Paso 1: count y draft, guardados en `state` (claves "count" y "draft"): sobreviven al giro y a la muerte del proceso.
//         addObservation() suma uno a count y vacía draft.
// Paso 2: cache, una propiedad normal del ViewModel (String?): sobrevive al giro, pero no a la muerte del proceso. Es lo
//         que quieres para algo caro que se puede volver a calcular.
class FieldNotesViewModel(private val state: SavedStateHandle) : ViewModel()
