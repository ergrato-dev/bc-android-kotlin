package dev.bitacora.vault

import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig2026
import com.google.crypto.tink.aead.PredefinedAeadParameters
import java.io.File
import java.nio.file.Files
import java.security.GeneralSecurityException
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Ya está hecho. No lo cambies: cambia Vault hasta que pasen los 9.
class VaultTest {
    private val dir = Files.createTempDirectory("vault").toFile()
    private val keyset = File(dir, "keyset.json")

    // En Android, la clave maestra es del Keystore. Aquí, una de Tink, en memoria.
    private fun masterKey(): Aead = KeysetHandle.generateNew(PredefinedAeadParameters.AES256_GCM, AeadConfig2026.get())
        .getPrimitive(AeadConfig2026.get(), Aead::class.java)

    private val master = masterKey()
    private val vault = Vault(keyset, master)
    private val token = "tk_live_4f9a2c7e1b"

    // Lo que se guarda no contiene el texto.
    @Test
    fun theTextIsNotThere() {
        val sealed = vault.seal(token, "token")
        assertFalse(token in sealed.toString(Charsets.ISO_8859_1))
    }

    // El mismo texto dos veces sale distinto: cada cifrado lleva su nonce, y quien mira no sabe si se repite.
    @Test
    fun twiceIsDifferent() {
        assertFalse(vault.seal(token, "token").contentEquals(vault.seal(token, "token")))
    }

    @Test
    fun itOpens() {
        assertEquals(token, vault.open(vault.seal(token, "token"), "token"))
    }

    // El contexto no se cifra, pero se comprueba: un texto cifrado para «nota» no se abre como «token».
    @Test
    fun anotherContextDoesNotOpenIt() {
        assertNull(vault.open(vault.seal(token, "note"), "token"))
    }

    // Un solo bit cambiado y no se abre: AEAD también dice si alguien lo ha tocado.
    @Test
    fun aTouchedByteDoesNotOpenIt() {
        val sealed = vault.seal(token, "token")
        sealed[sealed.lastIndex] = (sealed.last().toInt() xor 1).toByte()
        assertNull(vault.open(sealed, "token"))
    }

    // La clave se guarda, pero cifrada con la maestra: el archivo no la tiene en claro.
    @Test
    fun theKeysetIsEncrypted() {
        vault.seal(token, "token")
        val json = keyset.readText()
        assertTrue("encryptedKeyset" in json, json)
        assertFalse("keyData" in json, json)
    }

    // Otra Vault con el mismo archivo y la misma maestra (la app, al volver a abrirse) abre lo de antes.
    @Test
    fun itSurvivesARestart() {
        val sealed = vault.seal(token, "token")
        assertEquals(token, Vault(keyset, master).open(sealed, "token"))
    }

    // Sin la maestra (el archivo copiado a otro equipo), la clave no se puede leer.
    @Test
    fun anotherMasterKeyCannotReadTheKeyset() {
        vault.seal(token, "token")
        assertFailsWith<GeneralSecurityException> { Vault(keyset, masterKey()).open(byteArrayOf(1), "token") }
    }

    // Tras rotar, lo nuevo se cifra con otra clave (los 5 primeros bytes dicen cuál) y lo viejo se sigue abriendo.
    @Test
    fun oldTextsOpenAfterRotating() {
        val before = vault.seal(token, "token")
        vault.rotate()
        val after = vault.seal(token, "token")
        assertNotEquals(before.take(5), after.take(5))
        assertEquals(token, vault.open(before, "token"))
        assertEquals(token, Vault(keyset, master).open(after, "token"))
        assertContentEquals(before.take(1), after.take(1))
    }
}
