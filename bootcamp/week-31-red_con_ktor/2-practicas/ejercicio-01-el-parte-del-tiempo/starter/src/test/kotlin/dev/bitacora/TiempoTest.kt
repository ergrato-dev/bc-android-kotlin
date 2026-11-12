package dev.bitacora

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TiempoTest {
    private val requests = mutableListOf<HttpRequestData>()

    private fun api(status: HttpStatusCode = HttpStatusCode.OK, body: () -> String = { forecast }) = ForecastApi(
        forecastClient(
            MockEngine { request ->
                requests += request
                json(body(), status)
            },
            apiKey = "clave-de-prueba",
        ),
    )

    private fun MockRequestHandleScope.json(text: String, status: HttpStatusCode): HttpResponseData =
        respond(text, status, headersOf(HttpHeaders.ContentType, "application/json"))

    private val forecast = """
        {
          "place": "Lote 2",
          "generated": "2026-11-11T05:00",
          "hourly": [
            {"time": "06:00", "temp_c": 3.5, "rain_mm": 0.0, "wind_kmh": 12.0},
            {"time": "07:00", "temp_c": 1.0, "rain_mm": 0.8},
            {"time": "08:00", "temp_c": 4.0, "rain_mm": 0.5, "wind_kmh": 20.0, "gusts_kmh": 35.0}
          ]
        }
    """

    @Test
    fun theForecastIsRead() = runTest {
        val read = api().forecast(4.6, -74.08, 3)
        assertEquals("Lote 2", read.place)
        assertEquals(Hour("06:00", 3.5, 0.0, 12.0), read.hourly.first())
    }

    // Sin wind_kmh, el viento es null; no 0.0 (no es lo mismo "sin viento" que "no se sabe").
    @Test
    fun aMissingFieldIsNull() = runTest {
        assertNull(api().forecast(4.6, -74.08, 3).hourly[1].wind)
    }

    // generated y gusts_kmh no están en las clases: se ignoran, no rompen.
    @Test
    fun unknownFieldsAreIgnored() = runTest {
        assertEquals(3, api().forecast(4.6, -74.08, 3).hourly.size)
    }

    @Test
    fun theQueryGoesInTheUrl() = runTest {
        api().forecast(4.6, -74.08, 3)
        assertEquals("https://api.tiempo.example/v2/forecast?lat=4.6&lon=-74.08&hours=3", requests.single().url.toString())
        assertEquals(HttpMethod.Get, requests.single().method)
    }

    @Test
    fun theKeyGoesInAHeader() = runTest {
        api().forecast(4.6, -74.08, 3)
        assertEquals("clave-de-prueba", requests.single().headers["X-Api-Key"])
    }

    @Test
    fun theRainyHoursAreTheWetOnes() = runTest {
        val read = api().forecast(4.6, -74.08, 3)
        assertEquals(listOf("07:00", "08:00"), read.rainyHours())
        assertEquals(1.0, read.minTemperature())
    }

    // El aviso va en JSON, con los nombres del servidor.
    @Test
    fun aReportIsPostedAsJson() = runTest {
        api(HttpStatusCode.Created) { """{"id": 41}""" }.report(Report("Lote 2", -1.5))
        val request = requests.single()
        assertEquals(HttpMethod.Post, request.method)
        assertEquals("https://api.tiempo.example/v2/reports", request.url.toString())
        assertEquals("application/json", request.body.contentType?.toString())
        assertEquals("""{"place":"Lote 2","min_temp_c":-1.5}""", request.body.toByteArray().decodeToString())
    }

    @Test
    fun theReportGetsItsId() = runTest {
        assertEquals(41, api(HttpStatusCode.Created) { """{"id": 41}""" }.report(Report("Lote 2", -1.5)))
    }
}
