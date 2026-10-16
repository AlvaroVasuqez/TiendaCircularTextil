package cl.duoc.textilcircular.ui.screens.acceso

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.textilcircular.data.repository.DatosDeEjemplo
import cl.duoc.textilcircular.ui.components.BotonPrincipal
import cl.duoc.textilcircular.ui.components.CampoTexto
import cl.duoc.textilcircular.ui.components.EncabezadoMarca
import cl.duoc.textilcircular.ui.components.sacudir

/** Formulario de ingreso. */
@Composable
fun LoginScreen(
    onCrearCuenta: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val foco = LocalFocusManager.current
    val ingresar = {
        foco.clearFocus()
        viewModel.ingresar()
    }

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
                titulo = "TEXTIL CIRCULAR",
                subtitulo = "Ingresa para gestionar tu emprendimiento, tu catálogo y tus mensajes."
            )

            Column(
                modifier = Modifier.sacudir(uiState.intentosFallidos).padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CampoTexto(
                    valor = uiState.correo,
                    onValorChange = viewModel::onCorreoChange,
                    etiqueta = "Correo electrónico",
                    icono = Icons.Filled.Email,
                    error = uiState.errores[CampoLogin.CORREO],
                    tipoTeclado = KeyboardType.Email,
                    capitalizacion = KeyboardCapitalization.None
                )
                CampoTexto(
                    valor = uiState.contrasena,
                    onValorChange = viewModel::onContrasenaChange,
                    etiqueta = "Contraseña",
                    icono = Icons.Filled.Lock,
                    error = uiState.errores[CampoLogin.CONTRASENA],
                    esContrasena = true,
                    accionIme = ImeAction.Done,
                    onAccionIme = ingresar
                )
            }

            BotonPrincipal(
                texto = "Ingresar",
                icono = Icons.AutoMirrored.Filled.Login,
                cargando = uiState.cargando,
                onClick = ingresar
            )
            TextButton(
                onClick = onCrearCuenta,
                enabled = !uiState.cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("¿No tienes cuenta? Crear cuenta")
            }

            HorizontalDivider()

            OutlinedButton(
                onClick = viewModel::entrarConDemo,
                enabled = !uiState.cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Text("Entrar con datos de ejemplo", modifier = Modifier.padding(start = 8.dp))
            }
            Text(
                text = "Cuenta de prueba: ${DatosDeEjemplo.CORREO_DEMO} / ${DatosDeEjemplo.CONTRASENA_DEMO}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
