package cl.duoc.textilcircular.ui.screens.acceso

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.textilcircular.domain.validacion.Validador
import cl.duoc.textilcircular.ui.components.BotonPrincipal
import cl.duoc.textilcircular.ui.components.CampoTexto
import cl.duoc.textilcircular.ui.components.EncabezadoMarca
import cl.duoc.textilcircular.ui.components.sacudir

/** Formulario de registro de la emprendedora. Al crear la cuenta se envía una notificación de bienvenida. */
@Composable
fun RegistroScreen(
    onIrAIngresar: () -> Unit,
    viewModel: RegistroViewModel = viewModel(factory = RegistroViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val foco = LocalFocusManager.current
    val errorTerminos = uiState.errores[CampoRegistro.TERMINOS]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.widthIn(max = 480.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            EncabezadoMarca(
                titulo = "CREA TU CUENTA",
                subtitulo = "Solo te pedimos lo básico. Después podrás completar tu perfil."
            )

            Column(
                modifier = Modifier.sacudir(uiState.intentosFallidos).padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CampoTexto(
                    valor = uiState.nombre,
                    onValorChange = viewModel::onNombreChange,
                    etiqueta = "Nombre y apellido",
                    icono = Icons.Filled.Person,
                    error = uiState.errores[CampoRegistro.NOMBRE],
                    capitalizacion = KeyboardCapitalization.Words
                )
                CampoTexto(
                    valor = uiState.correo,
                    onValorChange = viewModel::onCorreoChange,
                    etiqueta = "Correo electrónico",
                    icono = Icons.Filled.Email,
                    error = uiState.errores[CampoRegistro.CORREO],
                    tipoTeclado = KeyboardType.Email,
                    capitalizacion = KeyboardCapitalization.None
                )
                CampoTexto(
                    valor = uiState.contrasena,
                    onValorChange = viewModel::onContrasenaChange,
                    etiqueta = "Contraseña",
                    icono = Icons.Filled.Lock,
                    error = uiState.errores[CampoRegistro.CONTRASENA],
                    textoApoyo = "Mínimo ${Validador.MIN_CONTRASENA} caracteres, con letras y números",
                    esContrasena = true
                )
                CampoTexto(
                    valor = uiState.confirmacion,
                    onValorChange = viewModel::onConfirmacionChange,
                    etiqueta = "Repite la contraseña",
                    icono = Icons.Filled.LockReset,
                    error = uiState.errores[CampoRegistro.CONFIRMACION],
                    esContrasena = true,
                    accionIme = ImeAction.Done,
                    onAccionIme = { foco.clearFocus() }
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = uiState.aceptaTerminos,
                            role = Role.Checkbox,
                            onValueChange = viewModel::onTerminosChange
                        )
                ) {
                    Checkbox(
                        checked = uiState.aceptaTerminos,
                        onCheckedChange = null,
                        colors = CheckboxDefaults.colors(uncheckedColor = if (errorTerminos != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = "Acepto que la Fundación use mis datos solo para el directorio (Ley 19.628 y Ley 21.719).",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                AnimatedVisibility(
                    visible = errorTerminos != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = errorTerminos.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }

            BotonPrincipal(
                texto = "Crear cuenta",
                icono = Icons.Filled.PersonAdd,
                cargando = uiState.cargando,
                onClick = {
                    foco.clearFocus()
                    viewModel.registrar()
                }
            )
            TextButton(onClick = onIrAIngresar, enabled = !uiState.cargando, modifier = Modifier.fillMaxWidth()) {
                Text("Ya tengo cuenta, quiero ingresar")
            }
        }
    }
}
