package cl.duoc.textilcircular.data.seguridad

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContrasenasTest {

    @Test
    fun laMismaContrasenaConDistintaSalDaHashesDistintos() {
        val h1 = Contrasenas.hash("hilos2024", Contrasenas.generarSal())
        val h2 = Contrasenas.hash("hilos2024", Contrasenas.generarSal())
        assertNotEquals(h1, h2)
    }

    @Test
    fun verificaSoloLaContrasenaCorrecta() {
        val sal = Contrasenas.generarSal()
        val guardado = Contrasenas.hash("hilos2024", sal)
        assertTrue(Contrasenas.coincide("hilos2024", sal, guardado))
        assertFalse(Contrasenas.coincide("hilos2025", sal, guardado))
    }
}
