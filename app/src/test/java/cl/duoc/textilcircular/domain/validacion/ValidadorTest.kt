package cl.duoc.textilcircular.domain.validacion

import cl.duoc.textilcircular.data.model.Servicio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** Pruebas de las reglas de validación. Se ejecutan con: gradlew test */
class ValidadorTest {

    @Test
    fun nombre() {
        assertNotNull(Validador.nombre(""))
        assertNotNull(Validador.nombre("  "))
        assertNotNull(Validador.nombre("Al"))
        assertNotNull(Validador.nombre("Ana123"))
        assertNull(Validador.nombre("María José Ñúñez"))
    }

    @Test
    fun correo() {
        assertNotNull(Validador.correo(""))
        assertNotNull(Validador.correo("ana@"))
        assertNotNull(Validador.correo("ana.correo.cl"))
        assertNotNull(Validador.correo("ana@correo"))
        assertNull(Validador.correo("ana.perez@correo.cl"))
        assertNull(Validador.correo(" ana@duocuc.cl "))
    }

    @Test
    fun contrasenaNueva() {
        assertNotNull(Validador.contrasenaNueva(""))
        assertNotNull(Validador.contrasenaNueva("abc12"))
        assertNotNull(Validador.contrasenaNueva("abcdefgh"))
        assertNotNull(Validador.contrasenaNueva("12345678"))
        assertNull(Validador.contrasenaNueva("hilos2024"))
    }

    @Test
    fun confirmacion() {
        assertNotNull(Validador.confirmacion("hilos2024", ""))
        assertNotNull(Validador.confirmacion("hilos2024", "hilos2025"))
        assertNull(Validador.confirmacion("hilos2024", "hilos2024"))
    }

    @Test
    fun terminos() {
        assertNotNull(Validador.terminos(false))
        assertNull(Validador.terminos(true))
    }

    @Test
    fun regionYComuna() {
        assertNotNull(Validador.region(""))
        assertNotNull(Validador.region("Región Inventada"))
        assertNull(Validador.region("Región de Valparaíso"))

        assertNotNull(Validador.comuna("Región de Valparaíso", ""))
        // Comuna de otra región: el caso de las fichas "invisibles".
        assertNotNull(Validador.comuna("Región de Valparaíso", "Temuco"))
        assertNull(Validador.comuna("Región de Valparaíso", "Viña del Mar"))
    }

    @Test
    fun servicios() {
        assertNull(Validador.servicios("", emptyList()))
        assertNotNull(Validador.servicios("Mi Taller", emptyList()))
        assertNull(Validador.servicios("Mi Taller", listOf(Servicio.REPARACION)))
        assertNotNull(
            Validador.servicios("Mi Taller", listOf(Servicio.REPARACION, Servicio.ARRIENDO, Servicio.EDUCACION))
        )
    }

    @Test
    fun telefono() {
        assertNull(Validador.telefono(""))
        assertNotNull(Validador.telefono("12345"))
        assertNotNull(Validador.telefono("221234567"))
        assertEquals("+56 9 1234 5678", Validador.normalizarTelefono("912345678"))
        assertEquals("+56 9 1234 5678", Validador.normalizarTelefono("+56 9 1234 5678"))
        assertEquals("+56 9 1234 5678", Validador.normalizarTelefono("56912345678"))
    }

    @Test
    fun instagram() {
        assertNull(Validador.instagram(""))
        assertNotNull(Validador.instagram("mi taller"))
        assertNotNull(Validador.instagram(".mitaller"))
        assertNotNull(Validador.instagram("mi..taller"))
        assertEquals("mi.taller", Validador.normalizarInstagram("@Mi.Taller"))
        assertEquals("mi_taller", Validador.normalizarInstagram("https://www.instagram.com/mi_taller/?igsh=abc"))
    }

    @Test
    fun trabajoCatalogo() {
        assertNotNull(Validador.fotoTrabajo(null))
        assertNull(Validador.fotoTrabajo("/data/foto.jpg"))
        assertNotNull(Validador.tituloTrabajo("ab"))
        assertNotNull(Validador.tituloTrabajo("a".repeat(Validador.MAX_TITULO + 1)))
        assertNull(Validador.tituloTrabajo("Bolso de mezclilla"))
        assertNotNull(Validador.descripcionTrabajo("corta"))
        assertNull(Validador.descripcionTrabajo("Bolso hecho con jeans reciclados."))
        assertNotNull(Validador.servicioTrabajo(null))
        assertNull(Validador.servicioTrabajo(Servicio.ACCESORIOS))
    }

    @Test
    fun mensaje() {
        assertNotNull(Validador.mensaje("   "))
        assertNotNull(Validador.mensaje("a".repeat(Validador.MAX_MENSAJE + 1)))
        assertNull(Validador.mensaje("Hola, ¿haces envíos?"))
    }
}
