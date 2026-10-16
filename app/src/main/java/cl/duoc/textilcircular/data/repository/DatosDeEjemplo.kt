package cl.duoc.textilcircular.data.repository

import cl.duoc.textilcircular.data.model.Antiguedad
import cl.duoc.textilcircular.data.model.Emprendimiento
import cl.duoc.textilcircular.data.model.Servicio

/**
 * Datos 100 % FICTICIOS (Ley 21.719 / 19.628). Imitan la estructura del Excel real del
 * directorio pero ningún nombre ni empresa corresponde a una persona real.
 */
object DatosDeEjemplo {

    const val CORREO_DEMO = "demo@textilcircular.cl"
    const val CONTRASENA_DEMO = "demo1234"

    val emprendimientos: List<Emprendimiento> = listOf(
        Emprendimiento(1, "Carla Muñoz", "Hilos que Vuelven", "Región de Valparaíso", "Viña del Mar",
            listOf(Servicio.ACCESORIOS, Servicio.REPARACION), Antiguedad.MAS_5,
            "Bolsos y accesorios hechos con telas rescatadas. También arreglos de ropa."),
        Emprendimiento(2, "Javiera Lagos", "Retazo Taller", "Región de Valparaíso", "Valparaíso",
            listOf(Servicio.EDUCACION, Servicio.DISENO_CONFECCION), Antiguedad.ENTRE_3_5,
            "Talleres de costura básica y confección a pedido con tela en desuso."),
        Emprendimiento(3, "Paula Reyes", "Segunda Vuelta", "Región Metropolitana", "Ñuñoa",
            listOf(Servicio.SEGUNDA_MANO), Antiguedad.ENTRE_1_3,
            "Ropa de segunda mano seleccionada, lavada y lista para usar."),
        Emprendimiento(4, "Daniela Fuentes", "Costura Consciente", "Región Metropolitana", "Maipú",
            listOf(Servicio.REPARACION, Servicio.ROPA_SUPRARRECICLADA), Antiguedad.MAS_5,
            "Arreglos, bastas y transformación de prendas antiguas en piezas nuevas."),
        Emprendimiento(5, "Marcela Vidal", "Vestir de Nuevo", "Región Metropolitana", "Providencia",
            listOf(Servicio.ARRIENDO, Servicio.SEGUNDA_MANO), Antiguedad.MENOS_1,
            "Arriendo de vestidos de fiesta y disfraces para no comprar ropa de un solo uso."),
        Emprendimiento(6, "Rosa Painemal", "Telar Kuyen", "Región de La Araucanía", "Temuco",
            listOf(Servicio.ACCESORIOS, Servicio.EDUCACION), Antiguedad.MAS_5,
            "Accesorios en telar con lanas recicladas y talleres para niñas y niños."),
        Emprendimiento(7, "Camila Torres", "Remiendo Sur", "Región de Los Lagos", "Puerto Montt",
            listOf(Servicio.REPARACION), Antiguedad.ENTRE_3_5,
            "Reparación de parkas, mochilas y ropa de trabajo."),
        Emprendimiento(8, "Valentina Ríos", "Moda Circular Valdivia", "Región de Los Ríos", "Valdivia",
            listOf(Servicio.ROPA_SUPRARRECICLADA, Servicio.DISENO_CONFECCION), Antiguedad.ENTRE_1_3,
            "Chaquetas y faldas diseñadas a partir de mezclilla en desuso."),
        Emprendimiento(9, "Francisca Araya", "Hebra Norte", "Región de Coquimbo", "La Serena",
            listOf(Servicio.ACCESORIOS), Antiguedad.ENTRE_1_3,
            "Carteras y monederos con retazos de tapicería."),
        Emprendimiento(10, "Antonia Saavedra", "Taller Reusa", "Región del Biobío", "Concepción",
            listOf(Servicio.EDUCACION, Servicio.SEGUNDA_MANO), Antiguedad.ENTRE_3_5,
            "Ferias de ropa usada y talleres de reparación en juntas de vecinos."),
        Emprendimiento(11, "Constanza Pérez", "Punto Aparte", "Región de O'Higgins", "Rancagua",
            listOf(Servicio.DISENO_CONFECCION), Antiguedad.MENOS_1,
            "Confección a pedido de ropa infantil con telas recuperadas."),
        Emprendimiento(12, "Ignacia Mora", "Austral Textil", "Región de Magallanes", "Punta Arenas",
            listOf(Servicio.REPARACION, Servicio.ACCESORIOS), Antiguedad.MAS_5,
            "Arreglos de ropa de abrigo y gorros hechos con lana reciclada.")
    )

    /** Conversaciones de ejemplo de la cuenta demo: (emprendimientoId, texto, esMio, minutos atrás). */
    val mensajesDemo: List<MensajeDemo> = listOf(
        MensajeDemo(1, "Hola Carla, vi tus bolsos en el directorio. ¿Haces envíos a Valparaíso?", true, 180),
        MensajeDemo(1, "¡Hola! Sí, envío a toda la región. ¿Qué modelo te interesa?", false, 150),
        MensajeDemo(1, "El bolso tote con retazos de camisa. ¿Cuánto cuesta?", true, 149),
        MensajeDemo(2, "Hola Javiera, ¿cuándo es el próximo taller de costura básica?", true, 1500),
        MensajeDemo(2, "Hola, el sábado a las 11:00 en Cerro Alegre. Quedan 4 cupos.", false, 1480)
    )

    data class MensajeDemo(val emprendimientoId: Int, val texto: String, val esMio: Boolean, val minutosAtras: Long)
}
