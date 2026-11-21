package dev.bitacora.vault

import com.google.crypto.tink.Aead
import java.io.File

// Guarda textos de la Bitácora (un token, una nota privada) para que solo esta app, en este equipo, pueda leerlos.
// Cada texto va con su contexto, el «para qué»: no se cifra, pero abrir un texto con otro contexto tiene que fallar.
// keyset: el archivo donde vive la clave de cifrado, cifrada a su vez con master. master es la clave maestra: en
// Android, una clave del Keystore que nunca sale de él; en los tests, otra clave de Tink.
//
// TODO: esta versión no cifra nada. Hazlo con Tink (teoría 01): AEAD con AES256_GCM, y la clave guardada en keyset,
// cifrada con master.
class Vault(private val keyset: File, private val master: Aead) {
    // El texto cifrado.
    fun seal(text: String, context: String): ByteArray = "$context:$text".encodeToByteArray()

    // El texto, o null si no se puede abrir: otro contexto, un byte cambiado o algo que no salió de aquí.
    fun open(sealed: ByteArray, context: String): String? = sealed.decodeToString().substringAfter(':')

    // Una clave nueva para lo que se cifre desde ahora. Lo cifrado antes se sigue abriendo.
    fun rotate() {
    }
}
