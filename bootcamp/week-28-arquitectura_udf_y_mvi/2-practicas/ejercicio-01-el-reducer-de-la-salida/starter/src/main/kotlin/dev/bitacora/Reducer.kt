package dev.bitacora

// El reducer: el estado de antes y lo que ha pasado dan el de después. Sin corrutinas, sin Android, sin nada que
// guardar. Lo que no cambia se devuelve tal cual: la misma instancia, no una copia igual.
// Paso 1: EditDraft cambia el borrador.
// Paso 2: Add añade el borrador sin espacios y lo vacía; ni vacío ni repetido ("gps" y "GPS" son lo mismo).
// Paso 3: Toggle marca o desmarca; al marcar la última, el efecto AllPacked (solo si antes no estaban todas).
// Paso 4: Remove la quita, guarda en removed cuál era y dónde, y pide OfferUndo; Undo la devuelve a su sitio y olvida
// removed.
// Paso 5: ClearDone quita las marcadas.
// Un nombre que no está no cambia nada.
@Suppress("UNUSED_PARAMETER")
fun reduce(state: ChecklistState, intent: ChecklistIntent): Next = Next(state)
