package cl.duoc.textilcircular.ui.screens.directorio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.textilcircular.ui.components.EstadoVacio

/** Ficha ampliada de un emprendimiento (lo que la web hoy no tiene). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetalleEmprendimientoScreen(
    onAbrirChat: (Int) -> Unit,
    viewModel: DetalleEmprendimientoViewModel = viewModel(factory = DetalleEmprendimientoViewModel.Factory)
) {
    val emprendimiento by viewModel.emprendimiento.collectAsStateWithLifecycle()

    val actual = emprendimiento
    if (actual == null) {
        EstadoVacio(
            icono = Icons.Filled.SearchOff,
            titulo = "Emprendimiento no encontrado",
            mensaje = "Vuelve al directorio e inténtalo de nuevo."
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.widthIn(max = 600.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = actual.empresa, style = MaterialTheme.typography.headlineMedium)
                    Text(text = actual.nombre, style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Place, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("${actual.comuna}, ${actual.region}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 6.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Antigüedad: ${actual.antiguedad.etiqueta}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 6.dp))
                    }
                }
            }

            Text(text = "Servicios", style = MaterialTheme.typography.titleLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                actual.servicios.forEach { servicio ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Text(
                            text = servicio.etiqueta,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            if (actual.descripcion.isNotBlank()) {
                Text(text = "Sobre el emprendimiento", style = MaterialTheme.typography.titleLarge)
                Text(text = actual.descripcion, style = MaterialTheme.typography.bodyLarge)
            }

            Button(
                onClick = { onAbrirChat(actual.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                Text(text = "Enviar mensaje a ${actual.nombre.substringBefore(' ')}", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
