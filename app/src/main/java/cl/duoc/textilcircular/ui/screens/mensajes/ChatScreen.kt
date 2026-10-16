package cl.duoc.textilcircular.ui.screens.mensajes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.textilcircular.data.model.Mensaje
import cl.duoc.textilcircular.domain.validacion.Validador
import cl.duoc.textilcircular.ui.components.EstadoVacio

/** Conversación con un emprendimiento: burbujas + caja de texto. Todo dentro de la app. */
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val borrador by viewModel.borrador.collectAsStateWithLifecycle()
    val estadoLista = rememberLazyListState()

    // Cada vez que llega un mensaje nuevo, la lista baja hasta el final con animación.
    LaunchedEffect(uiState.mensajes.size) {
        if (uiState.mensajes.isNotEmpty()) estadoLista.animateScrollToItem(uiState.mensajes.size - 1)
    }

    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text(text = uiState.empresa, style = MaterialTheme.typography.titleMedium)
                Text(text = uiState.nombre, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (uiState.cargado && uiState.mensajes.isEmpty()) {
                EstadoVacio(
                    icono = Icons.AutoMirrored.Filled.Chat,
                    titulo = "Escribe el primer mensaje",
                    mensaje = "Cuéntale a ${uiState.nombre.substringBefore(' ')} qué necesitas."
                )
            }
            LazyColumn(
                state = estadoLista,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // animateItem: cada burbuja nueva aparece con un fundido en vez de "saltar".
                items(uiState.mensajes, key = { it.id }) { mensaje -> Burbuja(mensaje, Modifier.animateItem()) }
            }
        }

        AnimatedVisibility(
            visible = uiState.escribiendo,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            IndicadorEscribiendo(nombre = uiState.nombre.substringBefore(' '))
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = borrador.texto,
                onValueChange = viewModel::onBorradorChange,
                placeholder = { Text("Escribe un mensaje...") },
                isError = borrador.error != null,
                supportingText = borrador.error?.let { error ->
                    { Text("$error (${borrador.texto.trim().length}/${Validador.MAX_MENSAJE})") }
                },
                maxLines = 4,
                modifier = Modifier.weight(1f)
            )
            FilledIconButton(
                onClick = viewModel::enviar,
                enabled = borrador.puedeEnviar,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
            }
        }
    }
}

/** "Carla está escribiendo…" con un parpadeo suave infinito. */
@Composable
private fun IndicadorEscribiendo(nombre: String) {
    val transicion = rememberInfiniteTransition(label = "escribiendo")
    val opacidad by transicion.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 700), RepeatMode.Reverse),
        label = "opacidad"
    )
    Text(
        text = "$nombre está escribiendo…",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp).alpha(opacidad)
    )
}

/** Mis mensajes a la derecha en turquesa; los del emprendimiento a la izquierda en gris. */
@Composable
private fun Burbuja(mensaje: Mensaje, modifier: Modifier = Modifier) {
    val alineacion = if (mensaje.esMio) Alignment.CenterEnd else Alignment.CenterStart
    val color = if (mensaje.esMio) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val colorTexto = if (mensaje.esMio) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = alineacion) {
        Surface(color = color, shape = MaterialTheme.shapes.medium, modifier = Modifier.widthIn(max = 300.dp)) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(text = mensaje.texto, style = MaterialTheme.typography.bodyLarge, color = colorTexto)
                Text(
                    text = mensaje.hora,
                    style = MaterialTheme.typography.labelMedium,
                    color = colorTexto,
                    modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
                )
            }
        }
    }
}
