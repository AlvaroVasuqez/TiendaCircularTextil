package cl.duoc.textilcircular.ui.screens.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.textilcircular.TextilCircularApplication
import cl.duoc.textilcircular.data.model.Antiguedad
import cl.duoc.textilcircular.data.model.PerfilUsuario
import cl.duoc.textilcircular.data.model.RegionesChile
import cl.duoc.textilcircular.data.model.Servicio
import cl.duoc.textilcircular.data.repository.CuentaRepository
import cl.duoc.textilcircular.domain.validacion.Validador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CampoPerfil { NOMBRE, REGION, COMUNA, SERVICIOS, TELEFONO, INSTAGRAM }

data class PerfilUiState(
    val cargando: Boolean = true,
    val perfil: PerfilUsuario = PerfilUsuario(),
    val comunasDisponibles: List<String> = emptyList(),
    val errores: Map<CampoPerfil, String> = emptyMap(),
    val intentoEnvio: Boolean = false,
    val intentosFallidos: Int = 0
)

/**
 * Perfil de la emprendedora guardado en Room. Región y comuna se eligen de listas y al cambiar
 * la región se limpia la comuna; el Validador además comprueba que la comuna sea de esa región.
 */
class PerfilViewModel(private val cuentas: CuentaRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val perfil = cuentas.sesion.filterNotNull().first()
            _uiState.update {
                it.copy(cargando = false, perfil = perfil, comunasDisponibles = RegionesChile.comunasDe(perfil.region))
            }
        }
    }

    fun onNombreChange(v: String) = actualizar(CampoPerfil.NOMBRE) { it.copy(nombre = v) }
    fun onEmpresaChange(v: String) = actualizar(CampoPerfil.SERVICIOS) { it.copy(empresa = v) }
    fun onComunaChange(v: String) = actualizar(CampoPerfil.COMUNA) { it.copy(comuna = v) }
    fun onAntiguedadChange(v: Antiguedad?) = actualizar(null) { it.copy(antiguedad = v) }
    fun onTelefonoChange(v: String) = actualizar(CampoPerfil.TELEFONO) { it.copy(telefono = v) }
    fun onInstagramChange(v: String) = actualizar(CampoPerfil.INSTAGRAM) { it.copy(instagram = v) }

    fun onRegionChange(region: String) {
        actualizar(CampoPerfil.REGION) { it.copy(region = region, comuna = "") }
        _uiState.update { it.copy(comunasDisponibles = RegionesChile.comunasDe(region)) }
    }

    fun onServicioToggle(servicio: Servicio) = actualizar(CampoPerfil.SERVICIOS) { perfil ->
        val actuales = perfil.servicios
        val nuevos = when {
            servicio in actuales -> actuales - servicio
            actuales.size < MAX_SERVICIOS -> actuales + servicio
            else -> actuales
        }
        perfil.copy(servicios = nuevos)
    }

    /** Devuelve true si se guardó; false si hay errores (quedan en uiState.errores). */
    fun guardar(): Boolean {
        val estado = _uiState.value
        if (estado.cargando) return false
        val errores = validar(estado.perfil)
        if (errores.isNotEmpty()) {
            _uiState.update { it.copy(errores = errores, intentoEnvio = true, intentosFallidos = it.intentosFallidos + 1) }
            return false
        }
        // Se guarda en formato estándar: "+56 9 1234 5678" y el usuario de Instagram sin "@" ni enlace.
        val p = estado.perfil
        val limpio = p.copy(
            nombre = p.nombre.trim(),
            empresa = p.empresa.trim(),
            telefono = if (p.telefono.isBlank()) "" else Validador.normalizarTelefono(p.telefono).orEmpty(),
            instagram = if (p.instagram.isBlank()) "" else Validador.normalizarInstagram(p.instagram).orEmpty()
        )
        _uiState.update { it.copy(perfil = limpio, errores = emptyMap(), intentoEnvio = false) }
        viewModelScope.launch { cuentas.guardarPerfil(limpio) }
        return true
    }

    fun cerrarSesion() {
        viewModelScope.launch { cuentas.cerrarSesion() }
    }

    private fun actualizar(campo: CampoPerfil?, cambio: (PerfilUsuario) -> PerfilUsuario) {
        _uiState.update { estado ->
            val nuevo = cambio(estado.perfil)
            val errores = when {
                estado.intentoEnvio -> validar(nuevo)
                campo != null -> estado.errores - campo
                else -> estado.errores
            }
            estado.copy(perfil = nuevo, errores = errores)
        }
    }

    private fun validar(p: PerfilUsuario): Map<CampoPerfil, String> = buildMap {
        Validador.nombre(p.nombre)?.let { put(CampoPerfil.NOMBRE, it) }
        Validador.region(p.region)?.let { put(CampoPerfil.REGION, it) }
        Validador.comuna(p.region, p.comuna)?.let { put(CampoPerfil.COMUNA, it) }
        Validador.servicios(p.empresa, p.servicios)?.let { put(CampoPerfil.SERVICIOS, it) }
        Validador.telefono(p.telefono)?.let { put(CampoPerfil.TELEFONO, it) }
        Validador.instagram(p.instagram)?.let { put(CampoPerfil.INSTAGRAM, it) }
    }

    companion object {
        const val MAX_SERVICIOS = Validador.MAX_SERVICIOS

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TextilCircularApplication
                PerfilViewModel(app.container.cuentaRepository)
            }
        }
    }
}
