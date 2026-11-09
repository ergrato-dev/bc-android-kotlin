package dev.bitacora

import java.io.File
import javax.inject.Inject

// Ya está hecho: la lista de la salida, con dos formas de guardarla.
interface ChecklistRepository {
    fun items(): List<String>

    fun add(item: String)
}

// En memoria: para empezar, y para los tests de otros.
class InMemoryChecklistRepository @Inject constructor() : ChecklistRepository {
    private val items = mutableListOf<String>()

    override fun items(): List<String> = items.toList()

    override fun add(item: String) {
        items += item
    }
}

// En un archivo, una línea por cosa, dentro de la carpeta de la salida. La carpeta no la sabe: se la dan.
class FileChecklistRepository @Inject constructor(private val dir: File) : ChecklistRepository {
    private val file get() = File(dir, "checklist.txt")

    override fun items(): List<String> = if (file.exists()) file.readLines() else emptyList()

    override fun add(item: String) {
        file.appendText("$item\n")
    }
}

// Quien usa la lista. No sabe cuál es: la recibe por el constructor, como el ViewModel de la Bitácora.
class Planner @Inject constructor(val repository: ChecklistRepository) {
    fun pack(item: String) = repository.add(item)

    fun packed(): List<String> = repository.items()
}
