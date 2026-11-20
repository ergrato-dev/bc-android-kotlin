package dev.bitacora

import dev.bitacora.sensors.Probe

// Ya está hecho: las sondas de la app. Nadie las crea con su constructor: Sensors.load, por el nombre.
class Humidity : Probe {
    override val name = "Humedad"

    override fun read() = 61.0
}

class Temperature : Probe {
    override val name = "Temperatura"

    override fun read() = 18.5
}
