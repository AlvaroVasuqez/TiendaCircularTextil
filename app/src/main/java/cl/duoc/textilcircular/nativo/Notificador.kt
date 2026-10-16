package cl.duoc.textilcircular.nativo

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import cl.duoc.textilcircular.MainActivity
import cl.duoc.textilcircular.R

/** Contrato que usan los repositorios; así la capa de datos no depende de la API de Android. */
interface Notificador {
    fun bienvenida(nombre: String)
    fun nuevoMensaje(remitente: String, texto: String)
}

/**
 * Recurso nativo: notificaciones del sistema.
 * En Android 13+ requiere el permiso POST_NOTIFICATIONS (se pide al abrir la app);
 * si la usuaria lo niega, la app sigue funcionando y simplemente no notifica.
 */
class NotificadorAndroid(private val contexto: Context) : Notificador {

    override fun bienvenida(nombre: String) = mostrar(
        canal = CANAL_CUENTA,
        id = ID_BIENVENIDA,
        titulo = "¡Bienvenida a Textil Circular!",
        texto = "Hola $nombre, tu cuenta quedó creada. Completa tu perfil y sube fotos de tus trabajos."
    )

    override fun nuevoMensaje(remitente: String, texto: String) = mostrar(
        canal = CANAL_MENSAJES,
        id = remitente.hashCode(),
        titulo = "Nuevo mensaje de $remitente",
        texto = texto
    )

    private fun mostrar(canal: String, id: Int, titulo: String, texto: String) {
        val tienePermiso = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!tienePermiso) return

        // Tocar la notificación abre la app (o la trae al frente si ya estaba abierta).
        val intent = Intent(contexto, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val alTocar = PendingIntent.getActivity(
            contexto, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificacion = NotificationCompat.Builder(contexto, canal)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(alTocar)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(contexto).notify(id, notificacion)
        } catch (_: SecurityException) {
            // El permiso se revocó justo ahora: no se notifica, pero la app no se cae.
        }
    }

    companion object {
        const val CANAL_MENSAJES = "mensajes"
        const val CANAL_CUENTA = "cuenta"
        private const val ID_BIENVENIDA = 1

        /** Los canales se crean una vez al iniciar la app (obligatorio desde Android 8). */
        fun crearCanales(contexto: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val gestor = contexto.getSystemService(NotificationManager::class.java)
            gestor.createNotificationChannel(
                NotificationChannel(CANAL_MENSAJES, "Mensajes", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Avisos cuando recibes un mensaje nuevo"
                }
            )
            gestor.createNotificationChannel(
                NotificationChannel(CANAL_CUENTA, "Mi cuenta", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Bienvenida y avisos de tu cuenta"
                }
            )
        }
    }
}
