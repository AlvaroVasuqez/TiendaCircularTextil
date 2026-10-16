package cl.duoc.textilcircular.ui.screens.catalogo

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.textilcircular.data.model.Servicio
import cl.duoc.textilcircular.domain.validacion.Validador
import cl.duoc.textilcircular.ui.components.BotonPrincipal
import cl.duoc.textilcircular.ui.components.CampoTexto
import cl.duoc.textilcircular.ui.components.ImagenArchivo
import cl.duoc.textilcircular.ui.components.SelectorDesplegable
import cl.duoc.textilcircular.ui.components.sacudir
import java.util.Locale

/**
 * Formulario para subir un trabajo al catálogo. Usa dos recursos nativos:
 * - Cámara: pide el permiso CAMERA en el momento de usarla y guarda la foto vía FileProvider.
 * - Galería: selector de fotos del sistema (no necesita permisos de almacenamiento).
 */
@Composable
fun NuevoTrabajoScreen(
    onGuardado: () -> Unit,
    viewModel: NuevoTrabajoViewModel = viewModel(factory = NuevoTrabajoViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    val lanzadorCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        viewModel.onResultadoCamara(exito)
    }
    val abrirCamara: () -> Unit = {
        try {
            lanzadorCamara.launch(viewModel.prepararUriCamara())
        } catch (_: ActivityNotFoundException) {
            viewModel.onCamaraNoDisponible()
        }
    }
    val lanzadorPermisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) abrirCamara() else viewModel.onPermisoCamaraDenegado()
    }
    val lanzadorGaleria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onFotoGaleria(uri)
    }

    LaunchedEffect(uiState.guardado) {
        if (uiState.guardado) onGuardado()
    }

    val errorFoto = uiState.errores[CampoTrabajo.FOTO]

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Foto del trabajo", style = MaterialTheme.typography.titleLarge)

            // Vista previa: cambia con animación entre "sin foto", "procesando" y la foto lista.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .border(
                        width = 2.dp,
                        color = if (errorFoto != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(2.dp)
            ) {
                AnimatedContent(
                    targetState = Triple(uiState.procesandoFoto, uiState.foto?.ruta, errorFoto != null),
                    transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.92f)) togetherWith fadeOut() },
                    label = "vistaPrevia"
                ) { (procesando, ruta, conError) ->
                    when {
                        procesando -> Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                            Text("Optimizando la foto…", modifier = Modifier.padding(top = 12.dp))
                        }
                        ruta != null -> ImagenArchivo(
                            ruta = ruta,
                            descripcion = "Vista previa de la foto",
                            ladoMaximo = 1280,
                            modifier = Modifier.fillMaxSize()
                        )
                        else -> Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Outlined.AddPhotoAlternate,
                                contentDescription = null,
                                tint = if (conError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                "Toma una foto o elígela de tu galería",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        val tienePermiso = ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_GRANTED
                        if (tienePermiso) abrirCamara() else lanzadorPermisoCamara.launch(Manifest.permission.CAMERA)
                    },
                    enabled = !uiState.procesandoFoto,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                    Text("Cámara", modifier = Modifier.padding(start = 8.dp))
                }
                OutlinedButton(
                    onClick = {
                        lanzadorGaleria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    enabled = !uiState.procesandoFoto,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                    Text("Galería", modifier = Modifier.padding(start = 8.dp))
                }
            }

            AnimatedVisibility(
                visible = errorFoto != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    Text(
                        text = errorFoto.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }

            // Retroalimentación de la optimización: cuánto se ahorró en datos.
            val foto = uiState.foto
            AnimatedVisibility(
                visible = foto != null && errorFoto == null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (foto != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Compress, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Foto optimizada: ${formatearTamano(foto.bytesOriginales)} → ${formatearTamano(foto.bytesFinales)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }

            Text("Datos del trabajo", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))
            CampoTexto(
                valor = uiState.titulo,
                onValorChange = viewModel::onTituloChange,
                etiqueta = "Título",
                icono = Icons.Filled.Title,
                error = uiState.errores[CampoTrabajo.TITULO],
                maxCaracteres = Validador.MAX_TITULO
            )
            CampoTexto(
                valor = uiState.descripcion,
                onValorChange = viewModel::onDescripcionChange,
                etiqueta = "Descripción",
                icono = Icons.Filled.Description,
                error = uiState.errores[CampoTrabajo.DESCRIPCION],
                textoApoyo = "Materiales, tallas, precio aproximado…",
                lineas = 3,
                maxCaracteres = Validador.MAX_DESCRIPCION
            )
            SelectorDesplegable(
                etiqueta = "Categoría",
                icono = Icons.Filled.Category,
                opciones = Servicio.entries.map { it.corta },
                seleccion = uiState.servicio?.corta ?: "",
                onSeleccion = { texto -> viewModel.onServicioChange(Servicio.entries.firstOrNull { it.corta == texto }) },
                textoError = uiState.errores[CampoTrabajo.SERVICIO]
            )

            BotonPrincipal(
                texto = "Guardar en mi catálogo",
                icono = Icons.Filled.Save,
                cargando = uiState.cargando,
                onClick = viewModel::guardar,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

private fun formatearTamano(bytes: Long): String {
    val kb = bytes / 1024.0
    return if (kb < 1024) String.format(Locale.getDefault(), "%.0f KB", kb)
    else String.format(Locale.getDefault(), "%.1f MB", kb / 1024)
}
