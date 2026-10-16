package cl.duoc.textilcircular.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cl.duoc.textilcircular.ui.navigation.AccesoNavHost
import cl.duoc.textilcircular.ui.navigation.Destino
import cl.duoc.textilcircular.ui.navigation.DestinoPrincipal
import cl.duoc.textilcircular.ui.navigation.TextilNavHost
import cl.duoc.textilcircular.ui.navigation.destinosPrincipales
import cl.duoc.textilcircular.ui.navigation.estaSeleccionado
import cl.duoc.textilcircular.ui.navigation.navegarPrincipal
import kotlinx.coroutines.launch

/** Tipo de navegación elegido según el ancho disponible (window size class). */
enum class TipoNavegacion { BARRA_INFERIOR, RIEL, CAJON_PERMANENTE }

/**
 * Composable raíz. Según la sesión guardada en Room muestra el acceso (ingresar / crear cuenta)
 * o la app principal, con un fundido entre ambos.
 */
@Composable
fun TextilCircularApp(
    windowSizeClass: WindowSizeClass,
    sesionViewModel: SesionViewModel = viewModel(factory = SesionViewModel.Factory)
) {
    val estado by sesionViewModel.estado.collectAsStateWithLifecycle()
    PedirPermisoNotificaciones()

    AnimatedContent(
        targetState = estado,
        transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(250)) },
        label = "sesion"
    ) { actual ->
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when (actual) {
                EstadoSesion.CARGANDO -> Box(contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                EstadoSesion.SIN_SESION -> AccesoNavHost(modifier = Modifier.fillMaxSize())
                EstadoSesion.CON_SESION -> AppPrincipal(windowSizeClass)
            }
        }
    }
}

/**
 * Notificaciones (Android 13+): se pide el permiso al abrir la app. Si la usuaria lo niega,
 * todo sigue funcionando; solo no llegan avisos.
 */
@Composable
private fun PedirPermisoNotificaciones() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val contexto = LocalContext.current
    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        val concedido = ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!concedido) lanzador.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

/**
 * App con sesión iniciada. Decide la estructura de la pantalla:
 * - Compact (teléfono vertical): barra de navegación inferior.
 * - Medium (teléfono horizontal / tablet chica): riel lateral.
 * - Expanded (tablet grande): cajón de navegación permanente.
 * El contenido (NavHost) es el mismo en los tres casos.
 */
@Composable
private fun AppPrincipal(windowSizeClass: WindowSizeClass) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val mostrarMensaje: (String) -> Unit = { mensaje ->
        scope.launch { snackbarHostState.showSnackbar(mensaje) }
    }

    val tipoNavegacion = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> TipoNavegacion.BARRA_INFERIOR
        WindowWidthSizeClass.Medium -> TipoNavegacion.RIEL
        else -> TipoNavegacion.CAJON_PERMANENTE
    }
    val columnasDirectorio = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> 1
        WindowWidthSizeClass.Medium -> 2
        else -> 3
    }

    val onDestinoSeleccionado: (DestinoPrincipal) -> Unit = { navController.navegarPrincipal(it.destino) }

    val contenido: @Composable () -> Unit = {
        Scaffold(
            topBar = {
                BarraSuperior(
                    titulo = Destino.tituloDe(rutaActual),
                    mostrarVolver = Destino.esSecundaria(rutaActual),
                    onVolver = { navController.navigateUp() }
                )
            },
            bottomBar = {
                if (tipoNavegacion == TipoNavegacion.BARRA_INFERIOR) {
                    BarraInferior(rutaActual, onDestinoSeleccionado)
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            TextilNavHost(
                navController = navController,
                columnasDirectorio = columnasDirectorio,
                onMostrarMensaje = mostrarMensaje,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }

    when (tipoNavegacion) {
        TipoNavegacion.CAJON_PERMANENTE -> {
            PermanentNavigationDrawer(
                drawerContent = { CajonNavegacion(rutaActual, onDestinoSeleccionado) }
            ) {
                contenido()
            }
        }

        TipoNavegacion.RIEL -> {
            Row(modifier = Modifier.fillMaxSize()) {
                RielNavegacion(rutaActual, onDestinoSeleccionado)
                Box(modifier = Modifier.weight(1f)) { contenido() }
            }
        }

        TipoNavegacion.BARRA_INFERIOR -> contenido()
    }
}

/** Barra superior turquesa con texto oscuro, como los botones del sitio web. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BarraSuperior(titulo: String, mostrarVolver: Boolean, onVolver: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text(titulo) },
        navigationIcon = {
            if (mostrarVolver) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

/** Barra inferior: la opción estándar en teléfonos (pantallas compactas). */
@Composable
private fun BarraInferior(rutaActual: String?, onSeleccion: (DestinoPrincipal) -> Unit) {
    NavigationBar {
        destinosPrincipales.forEach { item ->
            NavigationBarItem(
                selected = item.estaSeleccionado(rutaActual),
                onClick = { onSeleccion(item) },
                icon = { Icon(item.icono, contentDescription = item.etiqueta) },
                label = { Text(item.etiqueta) }
            )
        }
    }
}

/** Riel lateral: aprovecha el ancho extra en pantallas medianas sin tapar contenido. */
@Composable
private fun RielNavegacion(rutaActual: String?, onSeleccion: (DestinoPrincipal) -> Unit) {
    NavigationRail {
        Spacer(modifier = Modifier.height(8.dp))
        destinosPrincipales.forEach { item ->
            NavigationRailItem(
                selected = item.estaSeleccionado(rutaActual),
                onClick = { onSeleccion(item) },
                icon = { Icon(item.icono, contentDescription = item.etiqueta) },
                label = { Text(item.etiqueta) }
            )
        }
    }
}

/** Cajón permanente: en tablets grandes hay espacio para mostrar siempre el menú completo. */
@Composable
private fun CajonNavegacion(rutaActual: String?, onSeleccion: (DestinoPrincipal) -> Unit) {
    PermanentDrawerSheet(modifier = Modifier.width(260.dp)) {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            Text(
                text = "TEXTIL CIRCULAR",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp)
            )
            destinosPrincipales.forEach { item ->
                NavigationDrawerItem(
                    label = { Text(item.etiqueta) },
                    selected = item.estaSeleccionado(rutaActual),
                    onClick = { onSeleccion(item) },
                    icon = { Icon(item.icono, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    }
}
