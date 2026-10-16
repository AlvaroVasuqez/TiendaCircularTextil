package cl.duoc.textilcircular.ui.screens.catalogo

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.textilcircular.TextilCircularApplication
import cl.duoc.textilcircular.data.model.Servicio
import cl.duoc.textilcircular.data.model.TrabajoCatalogo
import cl.duoc.textilcircular.data.repository.CatalogoRepository
import cl.duoc.textilcircular.domain.validacion.Validador
import cl.duoc.textilcircular.nativo.CompresorImagen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Lista del catálogo. `null` mientras Room entrega la primera lectura (evita mostrar "vacío" por un instante). */
class CatalogoViewModel(private val catalogo: CatalogoRepository) : ViewModel() {

    val trabajos: StateFlow<List<TrabajoCatalogo>?> =
        catalogo.trabajos.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun eliminar(id: Long) {
        viewModelScope.launch { catalogo.eliminar(id) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TextilCircularApplication
                CatalogoViewModel(app.container.catalogoRepository)
            }
        }
    }
}

enum class CampoTrabajo { FOTO, TITULO, DESCRIPCION, SERVICIO }

data class FotoProcesada(val ruta: String, val bytesOriginales: Long, val bytesFinales: Long)

data class NuevoTrabajoUiState(
    val foto: FotoProcesada? = null,
    val procesandoFoto: Boolean = false,
    val titulo: String = "",
    val descripcion: String = "",
    val servicio: Servicio? = null,
    val errores: Map<CampoTrabajo, String> = emptyMap(),
    val cargando: Boolean = false,
    val guardado: Boolean = false,
    val intentoEnvio: Boolean = false,
    val intentosFallidos: Int = 0
)

/**
 * Formulario "Nuevo trabajo". La foto llega de la cámara o la galería, se comprime en segundo
 * plano y queda en el almacenamiento interno. Si la usuaria sale sin guardar, la foto se borra.
 */
class NuevoTrabajoViewModel(
    private val catalogo: CatalogoRepository,
    private val compresor: CompresorImagen
) : ViewModel() {

    private val _uiState = MutableStateFlow(NuevoTrabajoUiState())
    val uiState: StateFlow<NuevoTrabajoUiState> = _uiState.asStateFlow()

    /** Archivo donde la cámara escribirá la foto que se está tomando. */
    private var uriCamara: Uri? = null

    fun onTituloChange(v: String) = actualizar(CampoTrabajo.TITULO) { it.copy(titulo = v) }
    fun onDescripcionChange(v: String) = actualizar(CampoTrabajo.DESCRIPCION) { it.copy(descripcion = v) }
    fun onServicioChange(v: Servicio?) = actualizar(CampoTrabajo.SERVICIO) { it.copy(servicio = v) }

    fun prepararUriCamara(): Uri = compresor.crearUriParaCamara().also { uriCamara = it }

    fun onResultadoCamara(exito: Boolean) {
        val uri = uriCamara
        if (exito && uri != null) procesarFoto(uri) else compresor.limpiarTemporalesCamara()
    }

    fun onFotoGaleria(uri: Uri) = procesarFoto(uri)

    fun onPermisoCamaraDenegado() = mostrarErrorFoto(
        "Sin permiso de cámara no podemos tomar la foto. Puedes elegirla desde la galería o activar el permiso en Ajustes."
    )

    fun onCamaraNoDisponible() = mostrarErrorFoto("Este teléfono no tiene una app de cámara disponible. Usa la galería.")

    private fun mostrarErrorFoto(mensaje: String) {
        _uiState.update { it.copy(errores = it.errores + (CampoTrabajo.FOTO to mensaje)) }
    }

    private fun procesarFoto(uri: Uri) {
        _uiState.update { it.copy(procesandoFoto = true) }
        viewModelScope.launch {
            try {
                val resultado = withContext(Dispatchers.IO) { compresor.comprimir(uri) }
                val anterior = _uiState.value.foto
                compresor.eliminar(anterior?.ruta)
                actualizar(CampoTrabajo.FOTO) {
                    it.copy(
                        foto = FotoProcesada(resultado.ruta, resultado.bytesOriginales, resultado.bytesFinales),
                        procesandoFoto = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update { it.copy(procesandoFoto = false) }
                mostrarErrorFoto("No pudimos usar esa imagen. Intenta con otra foto.")
            } catch (_: OutOfMemoryError) {
                _uiState.update { it.copy(procesandoFoto = false) }
                mostrarErrorFoto("La imagen es demasiado grande para este teléfono. Intenta con otra.")
            } finally {
                compresor.limpiarTemporalesCamara()
            }
        }
    }

    fun guardar() {
        val estado = _uiState.value
        if (estado.cargando || estado.procesandoFoto) return
        val errores = validar(estado)
        if (errores.isNotEmpty()) {
            _uiState.update { it.copy(errores = errores, intentoEnvio = true, intentosFallidos = it.intentosFallidos + 1) }
            return
        }
        val foto = estado.foto ?: return
        val servicio = estado.servicio ?: return
        _uiState.update { it.copy(cargando = true) }
        viewModelScope.launch {
            catalogo.agregar(estado.titulo, estado.descripcion, servicio, foto.ruta)
            _uiState.update { it.copy(cargando = false, guardado = true) }
        }
    }

    private fun actualizar(campo: CampoTrabajo, cambio: (NuevoTrabajoUiState) -> NuevoTrabajoUiState) {
        _uiState.update { estado ->
            val nuevo = cambio(estado)
            nuevo.copy(errores = if (estado.intentoEnvio) validar(nuevo) else estado.errores - campo)
        }
    }

    private fun validar(e: NuevoTrabajoUiState): Map<CampoTrabajo, String> = buildMap {
        Validador.fotoTrabajo(e.foto?.ruta)?.let { put(CampoTrabajo.FOTO, it) }
        Validador.tituloTrabajo(e.titulo)?.let { put(CampoTrabajo.TITULO, it) }
        Validador.descripcionTrabajo(e.descripcion)?.let { put(CampoTrabajo.DESCRIPCION, it) }
        Validador.servicioTrabajo(e.servicio)?.let { put(CampoTrabajo.SERVICIO, it) }
    }

    override fun onCleared() {
        // Foto procesada pero el trabajo nunca se guardó: no dejar el archivo huérfano.
        if (!_uiState.value.guardado) compresor.eliminar(_uiState.value.foto?.ruta)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TextilCircularApplication
                NuevoTrabajoViewModel(app.container.catalogoRepository, app.container.compresorImagen)
            }
        }
    }
}
