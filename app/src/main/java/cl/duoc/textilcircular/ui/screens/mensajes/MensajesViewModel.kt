package cl.duoc.textilcircular.ui.screens.mensajes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.textilcircular.TextilCircularApplication
import cl.duoc.textilcircular.data.model.Mensaje
import cl.duoc.textilcircular.data.repository.DirectorioRepository
import cl.duoc.textilcircular.domain.validacion.Validador
import cl.duoc.textilcircular.ui.navigation.Destino
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Fila de la bandeja: una conversación con el nombre del emprendimiento y su último mensaje. */
data class ConversacionResumen(
    val emprendimientoId: Int,
    val empresa: String,
    val nombre: String,
    val ultimoTexto: String,
    val hora: String,
    val ultimoEsMio: Boolean
)

/** Bandeja de mensajes: une cada conversación con los datos del emprendimiento. `null` = cargando. */
class MensajesViewModel(repositorio: DirectorioRepository) : ViewModel() {

    val conversaciones: StateFlow<List<ConversacionResumen>?> = combine(
        repositorio.conversaciones,
        repositorio.emprendimientos
    ) { conversaciones, emprendimientos ->
        conversaciones.mapNotNull { conv ->
            val emp = emprendimientos.firstOrNull { it.id == conv.emprendimientoId } ?: return@mapNotNull null
            val ultimo = conv.ultimoMensaje ?: return@mapNotNull null
            ConversacionResumen(
                emprendimientoId = emp.id,
                empresa = emp.empresa,
                nombre = emp.nombre,
                ultimoTexto = ultimo.texto,
                hora = ultimo.hora,
                ultimoEsMio = ultimo.esMio
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TextilCircularApplication
                MensajesViewModel(app.container.directorioRepository)
            }
        }
    }
}

data class ChatUiState(
    val empresa: String = "",
    val nombre: String = "",
    val mensajes: List<Mensaje> = emptyList(),
    val escribiendo: Boolean = false,
    /** false hasta que Room entrega la primera lectura. */
    val cargado: Boolean = false
)

data class BorradorUiState(
    val texto: String = "",
    val error: String? = null
) {
    val puedeEnviar: Boolean get() = texto.isNotBlank() && error == null
}

/**
 * Una conversación. El id del emprendimiento llega por la ruta (SavedStateHandle).
 * El borrador vive aquí para sobrevivir rotaciones; su validación la hace el Validador.
 */
class ChatViewModel(
    savedStateHandle: SavedStateHandle,
    private val repositorio: DirectorioRepository
) : ViewModel() {

    private val emprendimientoId: Int = checkNotNull(savedStateHandle[Destino.ARG_EMPRENDIMIENTO_ID])
    private val emprendimiento = repositorio.obtenerEmprendimiento(emprendimientoId)

    private val _borrador = MutableStateFlow(BorradorUiState())
    val borrador: StateFlow<BorradorUiState> = _borrador

    val uiState: StateFlow<ChatUiState> = combine(
        repositorio.conversaciones.map { lista -> lista.firstOrNull { it.emprendimientoId == emprendimientoId } },
        repositorio.escribiendo
    ) { conv, escribiendo ->
        ChatUiState(
            empresa = emprendimiento?.empresa.orEmpty(),
            nombre = emprendimiento?.nombre.orEmpty(),
            mensajes = conv?.mensajes.orEmpty(),
            escribiendo = emprendimientoId in escribiendo,
            cargado = true
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ChatUiState(empresa = emprendimiento?.empresa.orEmpty(), nombre = emprendimiento?.nombre.orEmpty())
    )

    fun onBorradorChange(texto: String) {
        // Un borrador vacío no es un error mientras se escribe; solo el exceso de largo.
        _borrador.value = BorradorUiState(texto, if (texto.isBlank()) null else Validador.mensaje(texto))
    }

    fun enviar() {
        val texto = _borrador.value.texto
        if (Validador.mensaje(texto) != null) return
        _borrador.value = BorradorUiState()
        viewModelScope.launch { repositorio.enviarMensaje(emprendimientoId, texto) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TextilCircularApplication
                ChatViewModel(this.createSavedStateHandle(), app.container.directorioRepository)
            }
        }
    }
}
