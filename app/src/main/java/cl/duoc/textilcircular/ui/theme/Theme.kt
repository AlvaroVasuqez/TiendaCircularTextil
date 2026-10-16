package cl.duoc.textilcircular.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * Esquema claro: igual que el sitio web, fondo blanco, textos negros, turquesa como
 * color principal y amarillo como acento. Sobre turquesa y amarillo el texto va oscuro
 * porque ambos colores son claros (así lo hace también la página).
 */
private val EsquemaClaro = lightColorScheme(
    primary = Turquesa,
    onPrimary = TurquesaOscuro,
    primaryContainer = TurquesaClaro,
    onPrimaryContainer = TurquesaOscuro,
    secondary = Amarillo,
    onSecondary = AmarilloOscuro,
    secondaryContainer = AmarilloClaro,
    onSecondaryContainer = AmarilloOscuro,
    tertiary = Petroleo,
    onTertiary = Blanco,
    tertiaryContainer = PetroleoClaro,
    onTertiaryContainer = Blanco,
    background = Blanco,
    onBackground = Negro,
    surface = Blanco,
    onSurface = Negro,
    surfaceVariant = GrisFondo,
    onSurfaceVariant = GrisTexto,
    outline = GrisBorde
)

/** Esquema oscuro: usa los tonos petróleo del "hero" del sitio como fondo. */
private val EsquemaOscuro = darkColorScheme(
    primary = Turquesa,
    onPrimary = TurquesaOscuro,
    primaryContainer = TurquesaOscuro,
    onPrimaryContainer = TurquesaClaro,
    secondary = Amarillo,
    onSecondary = AmarilloOscuro,
    secondaryContainer = AmarilloOscuro,
    onSecondaryContainer = AmarilloClaro,
    tertiary = TurquesaClaro,
    onTertiary = PetroleoOscuro,
    tertiaryContainer = Petroleo,
    onTertiaryContainer = Blanco,
    background = PetroleoOscuro,
    onBackground = Blanco,
    surface = GrisOscuro,
    onSurface = Blanco,
    surfaceVariant = Petroleo,
    onSurfaceVariant = GrisClaroTexto,
    outline = GrisBorde
)

/** Esquinas redondeadas medianas, como las tarjetas del directorio web. */
private val Formas = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/**
 * Tema de la app. Sin color dinámico (Material You): la Fundación pidió respetar
 * sus colores corporativos en todos los equipos.
 */
@Composable
fun TextilCircularTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Tipografia,
        shapes = Formas,
        content = content
    )
}
