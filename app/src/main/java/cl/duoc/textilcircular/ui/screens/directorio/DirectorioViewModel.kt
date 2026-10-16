package cl.duoc.textilcircular.ui.screens.directorio

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.textilcircular.TextilCircularApplication
import cl.duoc.textilcircular.data.model.Emprendimiento
import cl.duoc.textilcircular.data.model.Servicio
import cl.duoc.textilcircular.data.repository.DirectorioRepository
import cl.duoc.textilcircular.ui.navigation.Destino
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/** Los tres filtros del directorio web: región, comuna y servicio (vacío = "Todas/Todos"). */
data class Filtros(
    val region: String = "",
    val comuna: String = "",
    val servicio: Servicio? = null
) {
    val hayFiltros: Boolean get() = region.isNotBlank() || comuna.isNotBlank() || servicio != null
}

data class DirectorioUiState(
    val resultados: List<Emprendimiento> = emptyList(),
    val filtros: Filtros = Filtros(),
    /** Solo las regiones y comunas que realmente tienen emprendimientos, igual que hace la web. */
    val regionesDisponibles: List<String> = emptyList(),
    val comunasDisponibles: List<String> = emptyList(),
    val total: Int = 0
)

/**
 * Replica la lógica de filtrado del sitio web (region === seleccionada, comuna === seleccionada,
 * servicios.includes(seleccionado)). Al cambiar la región se limpia la comuna.
 */
class DirectorioViewModel(private val repositorio: DirectorioRepository) : ViewModel() {

    private val _filtros = MutableStateFlow(Filtros())

    val uiState: StateFlow<DirectorioUiState> = combine(repositorio.emprendimientos, _filtros) { todos, filtros ->
        val filtrados = todos.filter { e ->
            (filtros.region.isBlank() || e.region == filtros.region) &&
                (filtros.comuna.isBlank() || e.comuna == filtros.comuna) &&
                (filtros.servicio == null || filtros.servicio in e.servicios)
        }
        val comunas = todos
            .filter { filtros.region.isBlank() || it.region == filtros.region }
            .map { it.comuna }.distinct().sorted()
        DirectorioUiState(
            resultados = filtrados.sortedBy { it.empresa },
            filtros = filtros,
            regionesDisponibles = todos.map { it.region }.distinct().sorted(),
            comunasDisponibles = comunas,
            total = todos.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DirectorioUiState())

    fun onRegionChange(region: String) = _filtros.update { it.copy(region = region, comuna = "") }
    fun onComunaChange(comuna: String) = _filtros.update { it.copy(comuna = comuna) }
    fun onServicioChange(servicio: Servicio?) = _filtros.update { it.copy(servicio = servicio) }
    fun limpiarFiltros() { _filtros.value = Filtros() }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TextilCircularApplication
                DirectorioViewModel(app.container.directorioRepository)
            }
        }
    }
}

/** Detalle de un emprendimiento. El id llega por la ruta y se lee desde SavedStateHandle. */
class DetalleEmprendimientoViewModel(
    savedStateHandle: SavedStateHandle,
    private val repositorio: DirectorioRepository
) : ViewModel() {

    private val emprendimientoId: Int = checkNotNull(savedStateHandle[Destino.ARG_EMPRENDIMIENTO_ID])

    val emprendimiento: StateFlow<Emprendimiento?> = repositorio.emprendimientos
        .map { lista -> lista.firstOrNull { it.id == emprendimientoId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), repositorio.obtenerEmprendimiento(emprendimientoId))

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TextilCircularApplication
                DetalleEmprendimientoViewModel(this.createSavedStateHandle(), app.container.directorioRepository)
            }
        }
    }
}
