package cl.duoc.textilcircular.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import cl.duoc.textilcircular.data.model.Servicio
import cl.duoc.textilcircular.nativo.CompresorImagen
import cl.duoc.textilcircular.ui.theme.Amarillo
import cl.duoc.textilcircular.ui.theme.Negro
import cl.duoc.textilcircular.ui.theme.Turquesa
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/** Etiqueta pequeña de servicio, como el "servicio-tag" amarillo de las tarjetas del sitio web. */
@Composable
fun EtiquetaServicio(servicio: Servicio, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.extraSmall,
        modifier = modifier
    ) {
        Text(
            text = servicio.corta,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/** Mensaje amigable cuando una lista está vacía. */
@Composable
fun EstadoVacio(
    icono: ImageVector,
    titulo: String,
    mensaje: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

/**
 * Ícono de error que aparece con una animación de escala cuando el campo tiene un problema.
 * Así el error no depende solo del color (accesibilidad).
 */
@Composable
private fun IconoError(visible: Boolean) {
    AnimatedVisibility(visible = visible, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
        Icon(Icons.Filled.Error, contentDescription = "Campo con error", tint = MaterialTheme.colorScheme.error)
    }
}

/**
 * Campo de texto estándar de los formularios: ícono que identifica el dato, ícono de error,
 * mensaje bajo el campo y contador opcional. El texto del error lo decide el ViewModel
 * (usando el Validador); este componente solo lo muestra.
 */
@Composable
fun CampoTexto(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    error: String? = null,
    textoApoyo: String? = null,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    capitalizacion: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    accionIme: ImeAction = ImeAction.Next,
    onAccionIme: (() -> Unit)? = null,
    esContrasena: Boolean = false,
    lineas: Int = 1,
    maxCaracteres: Int? = null,
    habilitado: Boolean = true
) {
    var contrasenaVisible by rememberSaveable { mutableStateOf(false) }
    val mensajeInferior = error ?: textoApoyo

    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(icono, contentDescription = null) },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconoError(visible = error != null)
                if (esContrasena) {
                    IconButton(onClick = { contrasenaVisible = !contrasenaVisible }) {
                        Icon(
                            if (contrasenaVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (contrasenaVisible) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(12.dp))
                }
            }
        },
        isError = error != null,
        supportingText = if (mensajeInferior != null || maxCaracteres != null) {
            {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = mensajeInferior.orEmpty(), modifier = Modifier.weight(1f))
                    if (maxCaracteres != null) Text("${valor.length}/$maxCaracteres")
                }
            }
        } else null,
        visualTransformation = if (esContrasena && !contrasenaVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            capitalization = if (esContrasena) KeyboardCapitalization.None else capitalizacion,
            autoCorrectEnabled = !esContrasena && tipoTeclado == KeyboardType.Text,
            keyboardType = if (esContrasena) KeyboardType.Password else tipoTeclado,
            imeAction = accionIme
        ),
        keyboardActions = if (onAccionIme != null) KeyboardActions(onDone = { onAccionIme() }) else KeyboardActions.Default,
        singleLine = lineas == 1,
        minLines = lineas,
        maxLines = if (lineas == 1) 1 else lineas + 3,
        enabled = habilitado,
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Lista desplegable de solo lectura (ExposedDropdownMenu de Material 3).
 * Equivale a los <select> de "Todas las regiones / comunas / servicios" del sitio web.
 * `opcionVacia` es el texto que representa "sin filtro" (por ejemplo "Todas las regiones").
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorDesplegable(
    etiqueta: String,
    opciones: List<String>,
    seleccion: String,
    onSeleccion: (String) -> Unit,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    opcionVacia: String? = null,
    habilitado: Boolean = true,
    textoError: String? = null,
    textoApoyo: String? = null
) {
    var expandido by remember { mutableStateOf(false) }
    val mensajeInferior = textoError ?: textoApoyo
    val textoMostrado = if (seleccion.isBlank() && opcionVacia != null) opcionVacia else seleccion

    ExposedDropdownMenuBox(
        expanded = expandido && habilitado,
        onExpandedChange = { if (habilitado) expandido = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = textoMostrado,
            onValueChange = {},
            readOnly = true,
            enabled = habilitado,
            label = { Text(etiqueta) },
            leadingIcon = icono?.let { { Icon(it, contentDescription = null) } },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconoError(visible = textoError != null)
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido && habilitado)
                }
            },
            isError = textoError != null,
            supportingText = mensajeInferior?.let { texto -> { Text(texto) } },
            singleLine = true,
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, habilitado)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expandido && habilitado,
            onDismissRequest = { expandido = false }
        ) {
            if (opcionVacia != null) {
                DropdownMenuItem(
                    text = { Text(opcionVacia) },
                    onClick = {
                        onSeleccion("")
                        expandido = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSeleccion(opcion)
                        expandido = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

/** Botón principal de los formularios: mientras trabaja muestra un indicador animado y no se puede volver a tocar. */
@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    cargando: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = !cargando,
        modifier = modifier.fillMaxWidth().height(52.dp)
    ) {
        AnimatedContent(
            targetState = cargando,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
            label = "botonCargando"
        ) { estaCargando ->
            if (estaCargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp,
                    color = LocalContentColor.current
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icono != null) {
                        Icon(icono, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(texto)
                }
            }
        }
    }
}

/**
 * Animación de "sacudida" horizontal: se dispara cada vez que `disparador` aumenta
 * (por ejemplo, al intentar guardar un formulario con errores).
 */
@Composable
fun Modifier.sacudir(disparador: Int): Modifier {
    val desplazamiento = remember { Animatable(0f) }
    LaunchedEffect(disparador) {
        if (disparador > 0) {
            for (x in listOf(-24f, 24f, -16f, 16f, -8f, 8f, 0f)) {
                desplazamiento.animateTo(x, animationSpec = tween(durationMillis = 45))
            }
        }
    }
    return this.offset { IntOffset(desplazamiento.value.roundToInt(), 0) }
}

/** Franja con el degradado turquesa → amarillo del sitio. Entra deslizándose al abrir la pantalla. */
@Composable
fun EncabezadoMarca(titulo: String, subtitulo: String, modifier: Modifier = Modifier) {
    val visible = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = visible,
        enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -it / 2 },
        modifier = modifier
    ) {
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
                Text(text = titulo, style = MaterialTheme.typography.headlineMedium, color = Negro)
                Text(text = subtitulo, style = MaterialTheme.typography.bodyMedium, color = Negro)
            }
        }
    }
}

/**
 * Muestra una foto guardada en el teléfono. Se lee en segundo plano y en tamaño reducido
 * (para no trabar equipos de gama baja) y aparece con un fundido.
 */
@Composable
fun ImagenArchivo(
    ruta: String,
    descripcion: String?,
    modifier: Modifier = Modifier,
    ladoMaximo: Int = 600
) {
    val imagen by produceState<ImageBitmap?>(initialValue = null, ruta, ladoMaximo) {
        value = withContext(Dispatchers.IO) { CompresorImagen.decodificar(ruta, ladoMaximo)?.asImageBitmap() }
    }
    Crossfade(targetState = imagen, label = "imagen", modifier = modifier) { bitmap ->
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = descripcion,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Image, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
