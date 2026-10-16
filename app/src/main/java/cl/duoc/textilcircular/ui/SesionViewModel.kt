package cl.duoc.textilcircular.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.textilcircular.TextilCircularApplication
import cl.duoc.textilcircular.data.repository.CuentaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

enum class EstadoSesion { CARGANDO, SIN_SESION, CON_SESION }

/** Observa en Room si hay una sesión abierta: así la sesión se mantiene al cerrar y abrir la app. */
class SesionViewModel(cuentas: CuentaRepository) : ViewModel() {

    val estado: StateFlow<EstadoSesion> = cuentas.sesion
        .map { if (it == null) EstadoSesion.SIN_SESION else EstadoSesion.CON_SESION }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, EstadoSesion.CARGANDO)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TextilCircularApplication
                SesionViewModel(app.container.cuentaRepository)
            }
        }
    }
}
