package cl.duoc.textilcircular.data.repository

import cl.duoc.textilcircular.data.local.CuentaDao
import cl.duoc.textilcircular.data.local.MensajeDao
import cl.duoc.textilcircular.data.local.MensajeEntity
import cl.duoc.textilcircular.data.model.Conversacion
import cl.duoc.textilcircular.data.model.Emprendimiento
import cl.duoc.textilcircular.data.model.Mensaje
import cl.duoc.textilcircular.nativo.Notificador
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * El directorio viene del dataset ficticio; los mensajes se guardan en Room.
 * Mientras no exista el backend, cada emprendimiento contesta con una respuesta automática
 * (simulación) para poder probar el flujo completo: mensaje → respuesta → notificación.
 */
class DirectorioRepositoryRoom(
    private val cuentaDao: CuentaDao,
    private val mensajeDao: MensajeDao,
    private val notificador: Notificador,
    private val alcanceApp: CoroutineScope
) : DirectorioRepository {

    override val emprendimientos: StateFlow<List<Emprendimiento>> =
        MutableStateFlow(DatosDeEjemplo.emprendimientos).asStateFlow()

    override val conversaciones: Flow<List<Conversacion>> = mensajeDao.observarDeSesion().map { mensajes ->
        mensajes.groupBy { it.emprendimientoId }
            .map { (emprendimientoId, lista) -> Conversacion(emprendimientoId, lista.map { it.aModelo() }) }
            .sortedByDescending { it.ultimoMensaje?.enviadoEn ?: 0L }
    }

    private val _escribiendo = MutableStateFlow<Set<Int>>(emptySet())
    override val escribiendo: StateFlow<Set<Int>> = _escribiendo.asStateFlow()

    override fun obtenerEmprendimiento(id: Int): Emprendimiento? =
        emprendimientos.value.firstOrNull { it.id == id }

    override suspend fun enviarMensaje(emprendimientoId: Int, texto: String) {
        val limpio = texto.trim()
        if (limpio.isEmpty()) return
        val cuentaId = cuentaDao.idSesionActiva() ?: return
        mensajeDao.insertar(
            MensajeEntity(
                cuentaId = cuentaId,
                emprendimientoId = emprendimientoId,
                texto = limpio,
                esMio = true,
                enviadoEn = System.currentTimeMillis()
            )
        )
        val emprendimiento = obtenerEmprendimiento(emprendimientoId) ?: return
        if (emprendimientoId in _escribiendo.value) return // ya viene una respuesta en camino

        // Se lanza en el alcance de la app: la respuesta llega aunque la usuaria salga del chat.
        alcanceApp.launch {
            _escribiendo.update { it + emprendimientoId }
            try {
                delay(DEMORA_RESPUESTA_MS)
                val respuesta = RESPUESTAS[(limpio.length) % RESPUESTAS.size]
                    .replace("{nombre}", emprendimiento.nombre.substringBefore(' '))
                mensajeDao.insertar(
                    MensajeEntity(
                        cuentaId = cuentaId,
                        emprendimientoId = emprendimientoId,
                        texto = respuesta,
                        esMio = false,
                        enviadoEn = System.currentTimeMillis()
                    )
                )
                notificador.nuevoMensaje(emprendimiento.empresa, respuesta)
            } finally {
                _escribiendo.update { it - emprendimientoId }
            }
        }
    }

    private fun MensajeEntity.aModelo() = Mensaje(
        id = id,
        texto = texto,
        esMio = esMio,
        hora = formatearHora(enviadoEn),
        enviadoEn = enviadoEn
    )

    /** Hoy: "14:05". Otro día: "12/10". */
    private fun formatearHora(milis: Long): String {
        val hoy = Calendar.getInstance()
        val fecha = Calendar.getInstance().apply { timeInMillis = milis }
        val mismoDia = hoy.get(Calendar.YEAR) == fecha.get(Calendar.YEAR) &&
            hoy.get(Calendar.DAY_OF_YEAR) == fecha.get(Calendar.DAY_OF_YEAR)
        val patron = if (mismoDia) "HH:mm" else "dd/MM"
        return SimpleDateFormat(patron, Locale.getDefault()).format(Date(milis))
    }

    companion object {
        private const val DEMORA_RESPUESTA_MS = 3_000L
        private val RESPUESTAS = listOf(
            "¡Hola! Soy {nombre}. Recibí tu mensaje y te respondo apenas termine en el taller.",
            "Gracias por escribir. Sí, puedo ayudarte con eso. ¿Me cuentas un poco más?",
            "¡Qué bueno que te interese! Te envío más detalles en un rato."
        )
    }
}
