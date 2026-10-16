package cl.duoc.textilcircular.ui.screens.catalogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.textilcircular.data.model.TrabajoCatalogo
import cl.duoc.textilcircular.ui.components.EstadoVacio
import cl.duoc.textilcircular.ui.components.EtiquetaServicio
import cl.duoc.textilcircular.ui.components.ImagenArchivo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Catálogo fotográfico de la emprendedora: lo que hoy la obliga a tener Instagram. */
@Composable
fun CatalogoScreen(
    onAgregar: () -> Unit,
    onMostrarMensaje: (String) -> Unit,
    viewModel: CatalogoViewModel = viewModel(factory = CatalogoViewModel.Factory)
) {
    val trabajos by viewModel.trabajos.collectAsStateWithLifecycle()
    var porEliminar by remember { mutableStateOf<TrabajoCatalogo?>(null) }
    val estadoGrilla = rememberLazyGridState()
    // El botón flotante se encoge (con animación) cuando la usuaria baja por la lista.
    val botonExpandido by remember { derivedStateOf { estadoGrilla.firstVisibleItemIndex == 0 } }

    Box(modifier = Modifier.fillMaxSize()) {
        val lista = trabajos
        when {
            lista == null -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            lista.isEmpty() -> EstadoVacio(
                icono = Icons.Filled.PhotoLibrary,
                titulo = "Tu catálogo está vacío",
                mensaje = "Toca \"Agregar trabajo\" y sube una foto de tus prendas, arreglos o talleres. Así tus clientas ven tu trabajo sin necesitar Instagram."
            )
            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                state = estadoGrilla,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = if (lista.size == 1) "1 trabajo publicado" else "${lista.size} trabajos publicados",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(lista, key = { it.id }) { trabajo ->
                    TarjetaTrabajo(
                        trabajo = trabajo,
                        onEliminar = { porEliminar = trabajo },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onAgregar,
            expanded = botonExpandido,
            icon = { Icon(Icons.Filled.AddAPhoto, contentDescription = null) },
            text = { Text("Agregar trabajo") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }

    porEliminar?.let { trabajo ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            title = { Text("¿Eliminar este trabajo?") },
            text = { Text("Se borrará \"${trabajo.titulo}\" y su foto de tu catálogo. No se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminar(trabajo.id)
                    porEliminar = null
                    onMostrarMensaje("Trabajo eliminado")
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { porEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun TarjetaTrabajo(trabajo: TrabajoCatalogo, onEliminar: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        ImagenArchivo(
            ruta = trabajo.rutaFoto,
            descripcion = "Foto de ${trabajo.titulo}",
            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
        )
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = trabajo.titulo, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            EtiquetaServicio(trabajo.servicio)
            Text(
                text = trabajo.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(trabajo.creadoEn)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar ${trabajo.titulo}", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
