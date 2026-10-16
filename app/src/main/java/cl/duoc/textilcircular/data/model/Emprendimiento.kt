package cl.duoc.textilcircular.data.model

/**
 * Las 7 categorías de servicio que usa el Directorio Textil Circular en su sitio web
 * (columna "servicios" del Excel). `etiqueta` es el texto oficial; `corta` se usa en
 * las etiquetas pequeñas de las tarjetas para que quepan en pantalla de celular.
 */
enum class Servicio(val etiqueta: String, val corta: String) {
    ACCESORIOS("Venta de accesorios confeccionados a partir de telas en desuso (suprareciclaje)", "Accesorios suprarreciclados"),
    DISENO_CONFECCION("Diseño y confección a pedido a partir de tela en desuso (suprareciclaje)", "Diseño y confección a pedido"),
    REPARACION("Reparación y modificación de ropa y accesorios", "Reparación y modificación"),
    EDUCACION("Educación y talleres", "Educación y talleres"),
    SEGUNDA_MANO("Venta de ropa de segunda mano", "Ropa de segunda mano"),
    ROPA_SUPRARRECICLADA("Venta de ropa confeccionada a partir de telas en desuso (suprareciclaje)", "Ropa suprarreciclada"),
    ARRIENDO("Arriendo de ropa (fiesta/disfraces/etc)", "Arriendo de ropa")
}

/** Columna "antiguedad" del Excel. */
enum class Antiguedad(val etiqueta: String) {
    MENOS_1("Menos de 1 año"),
    ENTRE_1_3("1-3 años"),
    ENTRE_3_5("3-5 años"),
    MAS_5("Más de 5 años")
}

/**
 * Una fila del Excel del directorio. Se conservan los mismos nombres de columna
 * (nombre, empresa, region, comuna, servicios, antiguedad) para que importar el
 * Excel real sea directo. Las columnas "telefono" e "instagram" existen en el Excel
 * pero la app NO las usa: el contacto es por mensajería interna.
 */
data class Emprendimiento(
    val id: Int,
    val nombre: String,
    val empresa: String,
    val region: String,
    val comuna: String,
    val servicios: List<Servicio>,
    val antiguedad: Antiguedad,
    val descripcion: String = ""
)

/** Un mensaje dentro de una conversación. `esMio` = lo escribió la usuaria de la app. */
data class Mensaje(
    val id: Long,
    val texto: String,
    val esMio: Boolean,
    val hora: String,
    val enviadoEn: Long
)

/** Conversación entre la usuaria de la app y un emprendimiento (una por emprendimiento). */
data class Conversacion(
    val emprendimientoId: Int,
    val mensajes: List<Mensaje> = emptyList()
) {
    val ultimoMensaje: Mensaje? get() = mensajes.lastOrNull()
}

/** Datos de la usuaria con sesión iniciada (los que pidió la Fundación + vías de contacto validadas). */
data class PerfilUsuario(
    val correo: String = "",
    val nombre: String = "",
    val empresa: String = "",
    val region: String = "",
    val comuna: String = "",
    val servicios: List<Servicio> = emptyList(),
    val antiguedad: Antiguedad? = null,
    val telefono: String = "",
    val instagram: String = ""
)

/** Un trabajo del catálogo fotográfico de la emprendedora. La foto vive en el almacenamiento interno. */
data class TrabajoCatalogo(
    val id: Long,
    val titulo: String,
    val descripcion: String,
    val servicio: Servicio,
    val rutaFoto: String,
    val creadoEn: Long
)
