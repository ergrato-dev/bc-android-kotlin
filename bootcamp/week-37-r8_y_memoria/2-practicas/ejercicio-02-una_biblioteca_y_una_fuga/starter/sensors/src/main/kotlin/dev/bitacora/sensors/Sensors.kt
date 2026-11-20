package dev.bitacora.sensors

// Ya está hecho. Una sonda mide una cosa. La app dice cuáles tiene con los nombres de sus clases, en un recurso:
// Sensors las crea por reflexión, como hacen WorkManager con los workers o Room con su _Impl.
interface Probe {
    val name: String

    fun read(): Double
}

object Sensors {
    fun load(classNames: List<String>): List<Probe> =
        classNames.map { Class.forName(it).getDeclaredConstructor().newInstance() as Probe }

    // Quien quiera enterarse de cada lectura se apunta aquí. Sensors vive lo que vive el proceso: lo que se apunta y
    // no se borra, también.
    private val listeners = mutableListOf<(String) -> Unit>()

    fun subscribe(listener: (String) -> Unit) {
        listeners += listener
    }

    fun unsubscribe(listener: (String) -> Unit) {
        listeners -= listener
    }

    fun publish(probes: List<Probe>) {
        for (probe in probes) {
            val reading = "${probe.name}: ${probe.read()}"
            listeners.forEach { it(reading) }
        }
    }
}
