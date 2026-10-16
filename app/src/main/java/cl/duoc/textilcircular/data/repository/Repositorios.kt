package cl.duoc.textilcircular.data.repository

import cl.duoc.textilcircular.data.model.Conversacion
import cl.duoc.textilcircular.data.model.Emprendimiento
import cl.duoc.textilcircular.data.model.PerfilUsuario
import cl.duoc.textilcircular.data.model.Servicio
import cl.duoc.textilcircular.data.model.TrabajoCatalogo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Contratos de la capa de datos (Repository del patrón MVVM).
 * Los ViewModels solo conocen estas interfaces; la implementación usa Room y mañana
 * podría sincronizar con la API REST de la Fundación sin tocar la UI.
 */

/** Cuenta y sesión de la emprendedora. `sesion` emite null cuando no hay nadie conectado. */
interface CuentaRepository {
    val sesion: Flow<PerfilUsuario?>

    /** Devuelve false si el correo ya tiene una cuenta. */
    suspend fun registrar(nombre: String, correo: String, contrasena: String): Boolean

    /** Devuelve false si el correo o la contraseña no coinciden. */
    suspend fun iniciarSesion(correo: String, contrasena: String): Boolean

    /** Entra con la cuenta de demostración (la crea con datos ficticios la primera vez). */
    suspend fun entrarConDemo()

    suspend fun cerrarSesion()

    suspend fun guardarPerfil(perfil: PerfilUsuario)
}

/** Directorio de emprendimientos y mensajería interna. */
interface DirectorioRepository {
    val emprendimientos: StateFlow<List<Emprendimiento>>
    val conversaciones: Flow<List<Conversacion>>

    /** Ids de los emprendimientos que están "escribiendo" una respuesta en este momento. */
    val escribiendo: StateFlow<Set<Int>>

    fun obtenerEmprendimiento(id: Int): Emprendimiento?

    suspend fun enviarMensaje(emprendimientoId: Int, texto: String)
}

/** Catálogo fotográfico de la emprendedora con sesión activa. */
interface CatalogoRepository {
    val trabajos: Flow<List<TrabajoCatalogo>>

    suspend fun agregar(titulo: String, descripcion: String, servicio: Servicio, rutaFoto: String)

    suspend fun eliminar(id: Long)
}
