package cl.duoc.textilcircular.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Todas las rutas de la app en un solo lugar. Usar objetos en vez de strings sueltos
 * evita errores de tipeo al navegar.
 */
sealed class Destino(val ruta: String, val titulo: String) {
    // Acceso (sin sesión)
    data object Login : Destino("login", "Ingresar")
    data object Registro : Destino("registro", "Crear cuenta")

    // Primer nivel (con sesión)
    data object Directorio : Destino("directorio", "Directorio")
    data object Catalogo : Destino("catalogo", "Mi catálogo")
    data object Mensajes : Destino("mensajes", "Mensajes")
    data object Perfil : Destino("perfil", "Mi perfil")

    /** Rutas con argumento: el id viaja dentro de la ruta. */
    data object DetalleEmprendimiento : Destino("directorio/{$ARG_EMPRENDIMIENTO_ID}", "Emprendimiento") {
        fun crearRuta(id: Int) = "directorio/$id"
    }

    /** Hay una conversación por emprendimiento, así que el chat se identifica por el id del emprendimiento. */
    data object Chat : Destino("mensajes/{$ARG_EMPRENDIMIENTO_ID}", "Conversación") {
        fun crearRuta(emprendimientoId: Int) = "mensajes/$emprendimientoId"
    }

    data object NuevoTrabajo : Destino("catalogo/nuevo", "Nuevo trabajo")

    companion object {
        const val ARG_EMPRENDIMIENTO_ID = "emprendimientoId"

        private val todos by lazy {
            listOf(Directorio, Catalogo, Mensajes, Perfil, DetalleEmprendimiento, Chat, NuevoTrabajo)
        }

        fun tituloDe(ruta: String?): String = todos.firstOrNull { it.ruta == ruta }?.titulo ?: "Textil Circular"

        /** Pantallas de segundo nivel: muestran flecha de "volver". */
        fun esSecundaria(ruta: String?): Boolean =
            ruta == DetalleEmprendimiento.ruta || ruta == Chat.ruta || ruta == NuevoTrabajo.ruta
    }
}

/** Elemento de la barra / riel / cajón de navegación. */
data class DestinoPrincipal(
    val destino: Destino,
    val etiqueta: String,
    val icono: ImageVector
)

/** Los 4 destinos de primer nivel. */
val destinosPrincipales = listOf(
    DestinoPrincipal(Destino.Directorio, "Directorio", Icons.Filled.Storefront),
    DestinoPrincipal(Destino.Catalogo, "Catálogo", Icons.Filled.PhotoLibrary),
    DestinoPrincipal(Destino.Mensajes, "Mensajes", Icons.AutoMirrored.Filled.Chat),
    DestinoPrincipal(Destino.Perfil, "Mi perfil", Icons.Filled.Person)
)

/** Un destino principal sigue "seleccionado" cuando estamos en una de sus subpantallas. */
fun DestinoPrincipal.estaSeleccionado(rutaActual: String?): Boolean =
    rutaActual != null && (rutaActual == destino.ruta || rutaActual.startsWith(destino.ruta + "/"))
