package cl.duoc.textilcircular.ui.screens.perfil

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.textilcircular.data.model.Antiguedad
import cl.duoc.textilcircular.data.model.RegionesChile
import cl.duoc.textilcircular.data.model.Servicio
import cl.duoc.textilcircular.ui.components.BotonPrincipal
import cl.duoc.textilcircular.ui.components.CampoTexto
import cl.duoc.textilcircular.ui.components.SelectorDesplegable
import cl.duoc.textilcircular.ui.components.sacudir

/** Formulario del perfil de la usuaria: los campos del Excel del directorio + vías de contacto validadas. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PerfilScreen(
    onMostrarMensaje: (String) -> Unit,
    viewModel: PerfilViewModel = viewModel(factory = PerfilViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val perfil = uiState.perfil
    val foco = LocalFocusManager.current
    var confirmarSalida by rememberSaveable { mutableStateOf(false) }

    if (uiState.cargando) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.widthIn(max = 600.dp).sacudir(uiState.intentosFallidos),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Email, contentDescription = null)
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text("Cuenta", style = MaterialTheme.typography.labelMedium)
                        Text(perfil.correo, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            TituloSeccion("Tus datos")
            CampoTexto(
                valor = perfil.nombre,
                onValorChange = viewModel::onNombreChange,
                etiqueta = "Nombre",
                icono = Icons.Filled.Person,
                error = uiState.errores[CampoPerfil.NOMBRE],
                capitalizacion = KeyboardCapitalization.Words
            )
            SelectorDesplegable(
                etiqueta = "Región",
                icono = Icons.Filled.Map,
                opciones = RegionesChile.nombres,
                seleccion = perfil.region,
                onSeleccion = viewModel::onRegionChange,
                textoError = uiState.errores[CampoPerfil.REGION]
            )
            SelectorDesplegable(
                etiqueta = "Ciudad / comuna",
                icono = Icons.Filled.LocationCity,
                opciones = uiState.comunasDisponibles,
                seleccion = perfil.comuna,
                onSeleccion = viewModel::onComunaChange,
                habilitado = perfil.region.isNotBlank(),
                textoError = uiState.errores[CampoPerfil.COMUNA],
                textoApoyo = if (perfil.region.isBlank()) "Primero elige tu región" else null
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            TituloSeccion("Tu emprendimiento (opcional)")
            CampoTexto(
                valor = perfil.empresa,
                onValorChange = viewModel::onEmpresaChange,
                etiqueta = "Nombre del emprendimiento",
                icono = Icons.Filled.Storefront
            )
            SelectorDesplegable(
                etiqueta = "Antigüedad",
                icono = Icons.Filled.Schedule,
                opciones = Antiguedad.entries.map { it.etiqueta },
                seleccion = perfil.antiguedad?.etiqueta ?: "",
                onSeleccion = { texto -> viewModel.onAntiguedadChange(Antiguedad.entries.firstOrNull { it.etiqueta == texto }) },
                opcionVacia = "Sin especificar"
            )
            Text(
                text = "Servicios (elige hasta ${PerfilViewModel.MAX_SERVICIOS})",
                style = MaterialTheme.typography.titleMedium
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Servicio.entries.forEach { servicio ->
                    val seleccionado = servicio in perfil.servicios
                    val bloqueado = !seleccionado && perfil.servicios.size >= PerfilViewModel.MAX_SERVICIOS
                    FilterChip(
                        selected = seleccionado,
                        enabled = !bloqueado,
                        onClick = { viewModel.onServicioToggle(servicio) },
                        label = { Text(servicio.corta) },
                        leadingIcon = if (seleccionado) {
                            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                        } else null
                    )
                }
            }
            MensajeError(uiState.errores[CampoPerfil.SERVICIOS])

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            TituloSeccion("Vías de contacto (opcional)")
            CampoTexto(
                valor = perfil.telefono,
                onValorChange = viewModel::onTelefonoChange,
                etiqueta = "Celular / WhatsApp",
                icono = Icons.Filled.Phone,
                error = uiState.errores[CampoPerfil.TELEFONO],
                textoApoyo = "Ejemplo: +56 9 1234 5678",
                tipoTeclado = KeyboardType.Phone
            )
            CampoTexto(
                valor = perfil.instagram,
                onValorChange = viewModel::onInstagramChange,
                etiqueta = "Instagram",
                icono = Icons.Filled.AlternateEmail,
                error = uiState.errores[CampoPerfil.INSTAGRAM],
                textoApoyo = "Tu usuario o el enlace a tu perfil. Lo revisamos para evitar enlaces rotos.",
                capitalizacion = KeyboardCapitalization.None,
                accionIme = ImeAction.Done,
                onAccionIme = { foco.clearFocus() }
            )

            BotonPrincipal(
                texto = "Guardar",
                icono = Icons.Filled.Save,
                onClick = {
                    foco.clearFocus()
                    if (viewModel.guardar()) onMostrarMensaje("Perfil guardado")
                    else onMostrarMensaje("Revisa los campos marcados en rojo")
                },
                modifier = Modifier.padding(top = 8.dp)
            )
            OutlinedButton(onClick = { confirmarSalida = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Text("Cerrar sesión", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Tus datos, catálogo y mensajes quedan guardados en este teléfono para cuando vuelvas a ingresar.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarSalida = false
                    viewModel.cerrarSesion()
                }) { Text("Cerrar sesión") }
            },
            dismissButton = { TextButton(onClick = { confirmarSalida = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun TituloSeccion(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = 4.dp)
    )
}

/** Error que no pertenece a un campo de texto (por ejemplo, los chips de servicios). */
@Composable
private fun MensajeError(texto: String?) {
    AnimatedVisibility(
        visible = texto != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
            Text(
                text = texto.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}
