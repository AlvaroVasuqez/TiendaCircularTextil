package cl.duoc.textilcircular.data.repository

import cl.duoc.textilcircular.data.local.CuentaDao
import cl.duoc.textilcircular.data.local.TrabajoDao
import cl.duoc.textilcircular.data.local.TrabajoEntity
import cl.duoc.textilcircular.data.model.Servicio
import cl.duoc.textilcircular.data.model.TrabajoCatalogo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

class CatalogoRepositoryRoom(
    private val cuentaDao: CuentaDao,
    private val trabajoDao: TrabajoDao
) : CatalogoRepository {

    override val trabajos: Flow<List<TrabajoCatalogo>> = trabajoDao.observarDeSesion().map { lista ->
        lista.map { entidad ->
            TrabajoCatalogo(
                id = entidad.id,
                titulo = entidad.titulo,
                descripcion = entidad.descripcion,
                servicio = Servicio.entries.firstOrNull { it.name == entidad.servicio } ?: Servicio.REPARACION,
                rutaFoto = entidad.rutaFoto,
                creadoEn = entidad.creadoEn
            )
        }
    }

    override suspend fun agregar(titulo: String, descripcion: String, servicio: Servicio, rutaFoto: String) {
        val cuentaId = cuentaDao.idSesionActiva() ?: return
        trabajoDao.insertar(
            TrabajoEntity(
                cuentaId = cuentaId,
                titulo = titulo.trim(),
                descripcion = descripcion.trim(),
                servicio = servicio.name,
                rutaFoto = rutaFoto,
                creadoEn = System.currentTimeMillis()
            )
        )
    }

    /** Borra el registro y también el archivo de la foto, para no dejar basura en el teléfono. */
    override suspend fun eliminar(id: Long) {
        val trabajo = trabajoDao.buscar(id) ?: return
        trabajoDao.eliminar(trabajo)
        withContext(Dispatchers.IO) { File(trabajo.rutaFoto).delete() }
    }
}
