package cl.duoc.textilcircular

import android.app.Application
import cl.duoc.textilcircular.data.AppContainer
import cl.duoc.textilcircular.data.AppContainerImpl
import cl.duoc.textilcircular.nativo.NotificadorAndroid

/** Punto de entrada de la app: crea el contenedor de dependencias y los canales de notificación. */
class TextilCircularApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this)
        NotificadorAndroid.crearCanales(this)
    }
}
