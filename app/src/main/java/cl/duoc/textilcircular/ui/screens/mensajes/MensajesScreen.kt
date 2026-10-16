package cl.duoc.textilcircular.ui.screens.mensajes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.textilcircular.ui.components.EstadoVacio

/** Bandeja de conversaciones. Reemplaza el contacto por WhatsApp/Instagram de la web. */
@Composable
fun MensajesScreen(
    onAbrirChat: (Int) -> Unit,
    viewModel: MensajesViewModel = viewModel(factory = MensajesViewModel.Factory)
) {
    val conversaciones = viewModel.conversaciones.collectAsStateWithLifecycle().value ?: return

    if (conversaciones.isEmpty()) {
        EstadoVacio(
            icono = Icons.AutoMirrored.Filled.Chat,
            titulo = "Aún no tienes conversaciones",
            mensaje = "Busca un emprendimiento en el directorio y toca \"Enviar mensaje\" para empezar a hablar."
        )
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(conversaciones, key = { it.emprendimientoId }) { conv ->
            Card(
                onClick = { onAbrirChat(conv.emprendimientoId) },
                // Cuando llega un mensaje, la conversación sube al primer lugar con animación.
                modifier = Modifier.fillMaxWidth().animateItem()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Filled.Storefront,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = conv.empresa, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(text = conv.hora, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(text = conv.nombre, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = if (conv.ultimoEsMio) "Tú: ${conv.ultimoTexto}" else conv.ultimoTexto,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
