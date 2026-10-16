package cl.duoc.textilcircular.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.textilcircular.ui.screens.acceso.LoginScreen
import cl.duoc.textilcircular.ui.screens.acceso.RegistroScreen
import cl.duoc.textilcircular.ui.screens.catalogo.CatalogoScreen
import cl.duoc.textilcircular.ui.screens.catalogo.NuevoTrabajoScreen
import cl.duoc.textilcircular.ui.screens.directorio.DetalleEmprendimientoScreen
import cl.duoc.textilcircular.ui.screens.directorio.DirectorioScreen
import cl.duoc.textilcircular.ui.screens.mensajes.ChatScreen
import cl.duoc.textilcircular.ui.screens.mensajes.MensajesScreen
import cl.duoc.textilcircular.ui.screens.perfil.PerfilScreen

/** Transiciones entre pantallas: la nueva entra deslizándose suavemente y la anterior se desvanece. */
private const val DURACION_TRANSICION = 300

private val entrar: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    fadeIn(tween(DURACION_TRANSICION)) + slideInHorizontally(tween(DURACION_TRANSICION)) { it / 5 }
}
private val salir: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    fadeOut(tween(DURACION_TRANSICION / 2))
}
private val volverEntrar: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    fadeIn(tween(DURACION_TRANSICION))
}
private val volverSalir: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    fadeOut(tween(DURACION_TRANSICION / 2)) + slideOutHorizontally(tween(DURACION_TRANSICION)) { it / 5 }
}

/**
 * Grafo de navegación principal (con sesión): asocia cada ruta con su pantalla.
 * Las pantallas no conocen el NavController; reciben lambdas (onX).
 */
@Composable
fun TextilNavHost(
    navController: NavHostController,
    columnasDirectorio: Int,
    onMostrarMensaje: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Destino.Directorio.ruta,
        enterTransition = entrar,
        exitTransition = salir,
        popEnterTransition = volverEntrar,
        popExitTransition = volverSalir,
        modifier = modifier
    ) {
        composable(Destino.Directorio.ruta) {
            DirectorioScreen(
                columnas = columnasDirectorio,
                onAbrirEmprendimiento = { id -> navController.navigate(Destino.DetalleEmprendimiento.crearRuta(id)) },
                onAbrirChat = { id -> navController.navigate(Destino.Chat.crearRuta(id)) }
            )
        }

        composable(
            route = Destino.DetalleEmprendimiento.ruta,
            arguments = listOf(navArgument(Destino.ARG_EMPRENDIMIENTO_ID) { type = NavType.IntType })
        ) {
            DetalleEmprendimientoScreen(
                onAbrirChat = { id -> navController.navigate(Destino.Chat.crearRuta(id)) }
            )
        }

        composable(Destino.Catalogo.ruta) {
            CatalogoScreen(
                onAgregar = { navController.navigate(Destino.NuevoTrabajo.ruta) },
                onMostrarMensaje = onMostrarMensaje
            )
        }

        composable(Destino.NuevoTrabajo.ruta) {
            NuevoTrabajoScreen(
                onGuardado = {
                    navController.popBackStack()
                    onMostrarMensaje("Trabajo agregado a tu catálogo")
                }
            )
        }

        composable(Destino.Mensajes.ruta) {
            MensajesScreen(
                onAbrirChat = { id -> navController.navigate(Destino.Chat.crearRuta(id)) }
            )
        }

        composable(
            route = Destino.Chat.ruta,
            arguments = listOf(navArgument(Destino.ARG_EMPRENDIMIENTO_ID) { type = NavType.IntType })
        ) {
            ChatScreen()
        }

        composable(Destino.Perfil.ruta) {
            PerfilScreen(onMostrarMensaje = onMostrarMensaje)
        }
    }
}

/** Grafo de acceso (sin sesión): ingresar y crear cuenta. Al iniciar sesión la app cambia sola al grafo principal. */
@Composable
fun AccesoNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Destino.Login.ruta,
        enterTransition = entrar,
        exitTransition = salir,
        popEnterTransition = volverEntrar,
        popExitTransition = volverSalir,
        modifier = modifier
    ) {
        composable(Destino.Login.ruta) {
            LoginScreen(onCrearCuenta = { navController.navigate(Destino.Registro.ruta) })
        }
        composable(Destino.Registro.ruta) {
            RegistroScreen(onIrAIngresar = { navController.popBackStack() })
        }
    }
}

/**
 * Navegación a un destino de primer nivel (barra/riel/cajón):
 * - popUpTo(Directorio) + saveState: no se apilan pestañas.
 * - launchSingleTop: tocar dos veces la misma pestaña no crea copias.
 * - restoreState: al volver a una pestaña se recupera su estado (scroll, filtros).
 */
fun NavHostController.navegarPrincipal(destino: Destino) {
    navigate(destino.ruta) {
        popUpTo(Destino.Directorio.ruta) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
