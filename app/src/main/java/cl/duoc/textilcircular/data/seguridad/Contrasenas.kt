package cl.duoc.textilcircular.data.seguridad

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Protección de contraseñas: se guarda SHA-256(sal + contraseña), nunca el texto.
 * La sal es aleatoria por cuenta, así dos cuentas con la misma clave tienen hashes distintos.
 */
object Contrasenas {

    fun generarSal(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.aHex()
    }

    fun hash(contrasena: String, sal: String): String =
        MessageDigest.getInstance("SHA-256").digest((sal + contrasena).toByteArray(Charsets.UTF_8)).aHex()

    /** Comparación en tiempo constante para no filtrar información por tiempos de respuesta. */
    fun coincide(contrasena: String, sal: String, hashGuardado: String): Boolean =
        MessageDigest.isEqual(hash(contrasena, sal).toByteArray(), hashGuardado.toByteArray())

    private fun ByteArray.aHex(): String = joinToString("") { "%02x".format(it) }
}
