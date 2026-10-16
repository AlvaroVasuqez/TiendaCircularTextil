package cl.duoc.textilcircular.data

import android.content.Context
import cl.duoc.textilcircular.data.local.TextilDatabase
import cl.duoc.textilcircular.data.repository.CatalogoRepository
import cl.duoc.textilcircular.data.repository.CatalogoRepositoryRoom
import cl.duoc.textilcircular.data.repository.CuentaRepository
import cl.duoc.textilcircular.data.repository.CuentaRepositoryRoom
import cl.duoc.textilcircular.data.repository.DirectorioRepository
import cl.duoc.textilcircular.data.repository.DirectorioRepositoryRoom
import cl.duoc.textilcircular.nativo.CompresorImagen
import cl.duoc.textilcircular.nativo.NotificadorAndroid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Contenedor de dependencias manual: una sola base de datos y un repositorio por tema, compartidos por los ViewModels. */
interface AppContainer {
    val cuentaRepository: CuentaRepository
    val directorioRepository: DirectorioRepository
    val catalogoRepository: CatalogoRepository
    val compresorImagen: CompresorImagen
}

class AppContainerImpl(contexto: Context) : AppContainer {
    private val appContext = contexto.applicationContext

    /** Vive mientras viva la app: tareas que no deben cancelarse al cambiar de pantalla. */
    private val alcanceApp = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val baseDatos by lazy { TextilDatabase.crear(appContext) }
    private val notificador by lazy { NotificadorAndroid(appContext) }

    override val cuentaRepository: CuentaRepository by lazy {
        CuentaRepositoryRoom(baseDatos.cuentaDao(), baseDatos.mensajeDao(), notificador)
    }
    override val directorioRepository: DirectorioRepository by lazy {
        DirectorioRepositoryRoom(baseDatos.cuentaDao(), baseDatos.mensajeDao(), notificador, alcanceApp)
    }
    override val catalogoRepository: CatalogoRepository by lazy {
        CatalogoRepositoryRoom(baseDatos.cuentaDao(), baseDatos.trabajoDao())
    }
    override val compresorImagen: CompresorImagen by lazy { CompresorImagen(appContext) }
}
