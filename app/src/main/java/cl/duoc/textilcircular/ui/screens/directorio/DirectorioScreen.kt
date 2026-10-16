package cl.duoc.textilcircular.ui.screens.directorio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.textilcircular.data.model.Emprendimiento
import cl.duoc.textilcircular.data.model.Servicio
import cl.duoc.textilcircular.ui.components.EtiquetaServicio
import cl.duoc.textilcircular.ui.components.SelectorDesplegable
import cl.duoc.textilcircular.ui.theme.Amarillo
import cl.duoc.textilcircular.ui.theme.Negro
import cl.duoc.textilcircular.ui.theme.Turquesa

/**
 * Pantalla principal: la versión móvil del directorio web.
 * Mismo encabezado, mismos tres filtros y tarjetas con los mismos campos
 * (empresa, nombre, comuna + región, servicios). El botón de contacto abre el chat interno
 * en lugar de WhatsApp o Instagram.
 */
@Composable
fun DirectorioScreen(
    columnas: Int,
    onAbrirEmprendimiento: (Int) -> Unit,
    onAbrirChat: (Int) -> Unit,
    viewModel: DirectorioViewModel = viewModel(factory = DirectorioViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyVerticalGrid(
        columns = GridCells.Fixed(columnas),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Encabezado y filtros ocupan todo el ancho, sin importar el número de columnas.
        item(span = { GridItemSpan(maxLineSpan) }) {
            Encabezado(total = uiState.total)
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Filtros(
                uiState = uiState,
                onRegionChange = viewModel::onRegionChange,
                onComunaChange = viewModel::onComunaChange,
                onServicioChange = viewModel::onServicioChange,
                onLimpiar = viewModel::limpiarFiltros
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            val n = uiState.resultados.size
            Text(
                text = if (n == 1) "1 emprendimiento encontrado" else "$n emprendimientos encontrados",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (uiState.resultados.isEmpty() && uiState.total > 0) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.SearchOff, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                    Text("No se encontraron resultados", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
                    Text("Prueba con otra región, comuna o servicio.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        items(uiState.resultados, key = { it.id }) { emprendimiento ->
            TarjetaEmprendimiento(
                emprendimiento = emprendimiento,
                onClick = { onAbrirEmprendimiento(emprendimiento.id) },
                onEnviarMensaje = { onAbrirChat(emprendimiento.id) },
                // Al filtrar, las tarjetas entran, salen y se reacomodan con animación.
                modifier = Modifier.animateItem()
            )
        }
    }
}

/** Franja con el degradado turquesa → amarillo del sitio y el título del directorio. */
@Composable
private fun Encabezado(total: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(listOf(Turquesa, Amarillo)),
                shape = MaterialTheme.shapes.medium
            )
            .padding(20.dp)
    ) {
        Column {
            Text(text = "DIRECTORIO TEXTIL CIRCULAR", style = MaterialTheme.typography.headlineMedium, color = Negro)
            Text(text = "Conecta con tu territorio", style = MaterialTheme.typography.titleMedium, color = Negro)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tenemos $total emprendimientos en todo Chile. Escríbeles directamente desde la app.",
                style = MaterialTheme.typography.bodyMedium,
                color = Negro
            )
        }
    }
}

@Composable
private fun Filtros(
    uiState: DirectorioUiState,
    onRegionChange: (String) -> Unit,
    onComunaChange: (String) -> Unit,
    onServicioChange: (Servicio?) -> Unit,
    onLimpiar: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SelectorDesplegable(
            etiqueta = "Región",
            opciones = uiState.regionesDisponibles,
            seleccion = uiState.filtros.region,
            onSeleccion = onRegionChange,
            icono = Icons.Filled.Map,
            opcionVacia = "Todas las regiones"
        )
        SelectorDesplegable(
            etiqueta = "Comuna",
            opciones = uiState.comunasDisponibles,
            seleccion = uiState.filtros.comuna,
            onSeleccion = onComunaChange,
            icono = Icons.Filled.LocationCity,
            opcionVacia = "Todas las comunas"
        )
        SelectorDesplegable(
            etiqueta = "Servicio",
            opciones = Servicio.entries.map { it.corta },
            seleccion = uiState.filtros.servicio?.corta ?: "",
            onSeleccion = { texto -> onServicioChange(Servicio.entries.firstOrNull { it.corta == texto }) },
            icono = Icons.Filled.Category,
            opcionVacia = "Todos los servicios"
        )
        AnimatedVisibility(
            visible = uiState.filtros.hayFiltros,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier.align(Alignment.End)
        ) {
            TextButton(onClick = onLimpiar) {
                Icon(Icons.Filled.FilterAltOff, contentDescription = null)
                Text("Limpiar filtros", modifier = Modifier.padding(start = 6.dp))
            }
        }
    }
}

/** Tarjeta con los mismos campos que la tarjeta del sitio web. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TarjetaEmprendimiento(
    emprendimiento: Emprendimiento,
    onClick: () -> Unit,
    onEnviarMensaje: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        // Borde superior turquesa, como el "border-top: 6px solid #05c9cd" de la web.
        Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(MaterialTheme.colorScheme.primary))
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = emprendimiento.empresa, style = MaterialTheme.typography.titleLarge)
            Text(text = emprendimiento.nombre, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider(color = MaterialTheme.colorScheme.primary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text(
                    text = "${emprendimiento.comuna}, ${emprendimiento.region}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                emprendimiento.servicios.forEach { EtiquetaServicio(it) }
            }
            Button(onClick = onEnviarMensaje, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                Text(text = "Enviar mensaje", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
