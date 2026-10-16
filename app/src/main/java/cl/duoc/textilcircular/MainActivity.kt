package cl.duoc.textilcircular

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import cl.duoc.textilcircular.ui.TextilCircularApp
import cl.duoc.textilcircular.ui.theme.TextilCircularTheme

/**
 * Única Activity de la app (patrón single-activity).
 * Calcula la clase de tamaño de ventana y se la entrega al composable raíz,
 * que decide si usa barra inferior, riel o cajón de navegación.
 */
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            TextilCircularTheme {
                TextilCircularApp(windowSizeClass = windowSizeClass)
            }
        }
    }
}
