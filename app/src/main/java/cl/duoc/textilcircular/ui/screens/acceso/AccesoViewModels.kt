package cl.duoc.textilcircular.ui.screens.acceso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.textilcircular.TextilCircularApplication
import cl.duoc.textilcircular.data.repository.CuentaRepository
import cl.duoc.textilcircular.domain.validacion.Validador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/*
 * Patrón común de los formularios:
 * - Antes del primer intento de envío, escribir en un campo solo borra SU error.
 * - Después del primer intento, cada cambio vuelve a validar todo (el error desaparece apenas se corrige).
 * - `intentosFallidos` aumenta en cada envío rechazado y dispara la animación de sacudida.
 * Las reglas viven en Validador; aquí solo se decide cuándo aplicarlas.
 */

// ---------------------------------------------------------------- Login

enum class CampoLogin { CORREO, CONTRASENA }

data class LoginUiState(
    val correo: String = "",
    val contrasena: String = "",
    val errores: Map<CampoLogin, String> = emptyMap(),
    val cargando: Boolean = false,
    val intentoEnvio: Boolean = false,
    val intentosFallidos: Int = 0
)

class LoginViewModel(private val cuentas: CuentaRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoChange(v: String) = actualizar(CampoLogin.CORREO) { it.copy(correo = v) }
    fun onContrasenaChange(v: String) = actualizar(CampoLogin.CONTRASENA) { it.copy(contrasena = v) }

    fun ingresar() {
        val estado = _uiState.value
        if (estado.cargando) return
        val errores = validar(estado)
        if (errores.isNotEmpty()) {
            _uiState.update { it.copy(errores = errores, intentoEnvio = true, intentosFallidos = it.intentosFallidos + 1) }
            return
        }
        _uiState.update { it.copy(cargando = true) }
        viewModelScope.launch {
            val ok = cuentas.iniciarSesion(estado.correo, estado.contrasena)
            // Si entra, la app cambia sola a la pantalla principal (la sesión se observa desde Room).
            if (!ok) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        intentoEnvio = false,
                        errores = mapOf(CampoLogin.CONTRASENA to "Correo o contraseña incorrectos"),
                        intentosFallidos = it.intentosFallidos + 1
                    )
                }
            }
        }
    }

    fun entrarConDemo() {
        if (_uiState.value.cargando) return
        _uiState.update { it.copy(cargando = true) }
        viewModelScope.launch { cuentas.entrarConDemo() }
    }

    private fun actualizar(campo: CampoLogin, cambio: (LoginUiState) -> LoginUiState) {
        _uiState.update { estado ->
            val nuevo = cambio(estado)
            nuevo.copy(errores = if (estado.intentoEnvio) validar(nuevo) else estado.errores - campo)
        }
    }

    private fun validar(e: LoginUiState): Map<CampoLogin, String> = buildMap {
        Validador.correo(e.correo)?.let { put(CampoLogin.CORREO, it) }
        Validador.contrasenaIngreso(e.contrasena)?.let { put(CampoLogin.CONTRASENA, it) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TextilCircularApplication
                LoginViewModel(app.container.cuentaRepository)
            }
        }
    }
}

// ---------------------------------------------------------------- Registro

enum class CampoRegistro { NOMBRE, CORREO, CONTRASENA, CONFIRMACION, TERMINOS }

data class RegistroUiState(
    val nombre: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val confirmacion: String = "",
    val aceptaTerminos: Boolean = false,
    val errores: Map<CampoRegistro, String> = emptyMap(),
    val cargando: Boolean = false,
    val intentoEnvio: Boolean = false,
    val intentosFallidos: Int = 0
)

class RegistroViewModel(private val cuentas: CuentaRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    fun onNombreChange(v: String) = actualizar(CampoRegistro.NOMBRE) { it.copy(nombre = v) }
    fun onCorreoChange(v: String) = actualizar(CampoRegistro.CORREO) { it.copy(correo = v) }
    fun onContrasenaChange(v: String) = actualizar(CampoRegistro.CONTRASENA) { it.copy(contrasena = v) }
    fun onConfirmacionChange(v: String) = actualizar(CampoRegistro.CONFIRMACION) { it.copy(confirmacion = v) }
    fun onTerminosChange(v: Boolean) = actualizar(CampoRegistro.TERMINOS) { it.copy(aceptaTerminos = v) }

    fun registrar() {
        val estado = _uiState.value
        if (estado.cargando) return
        val errores = validar(estado)
        if (errores.isNotEmpty()) {
            _uiState.update { it.copy(errores = errores, intentoEnvio = true, intentosFallidos = it.intentosFallidos + 1) }
            return
        }
        _uiState.update { it.copy(cargando = true) }
        viewModelScope.launch {
            val ok = cuentas.registrar(estado.nombre, estado.correo, estado.contrasena)
            if (!ok) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        intentoEnvio = false,
                        errores = mapOf(CampoRegistro.CORREO to "Este correo ya tiene una cuenta. Ingresa con él."),
                        intentosFallidos = it.intentosFallidos + 1
                    )
                }
            }
        }
    }

    private fun actualizar(campo: CampoRegistro, cambio: (RegistroUiState) -> RegistroUiState) {
        _uiState.update { estado ->
            val nuevo = cambio(estado)
            nuevo.copy(errores = if (estado.intentoEnvio) validar(nuevo) else estado.errores - campo)
        }
    }

    private fun validar(e: RegistroUiState): Map<CampoRegistro, String> = buildMap {
        Validador.nombre(e.nombre)?.let { put(CampoRegistro.NOMBRE, it) }
        Validador.correo(e.correo)?.let { put(CampoRegistro.CORREO, it) }
        Validador.contrasenaNueva(e.contrasena)?.let { put(CampoRegistro.CONTRASENA, it) }
        Validador.confirmacion(e.contrasena, e.confirmacion)?.let { put(CampoRegistro.CONFIRMACION, it) }
        Validador.terminos(e.aceptaTerminos)?.let { put(CampoRegistro.TERMINOS, it) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TextilCircularApplication
                RegistroViewModel(app.container.cuentaRepository)
            }
        }
    }
}
