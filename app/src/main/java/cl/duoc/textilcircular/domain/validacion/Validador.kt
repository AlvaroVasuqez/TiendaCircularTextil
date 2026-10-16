package cl.duoc.textilcircular.domain.validacion

import cl.duoc.textilcircular.data.model.RegionesChile
import cl.duoc.textilcircular.data.model.Servicio

/**
 * Todas las reglas de validación de la app en un solo lugar, sin dependencias de Android ni de
 * Compose (se prueban con JUnit en ValidadorTest). Cada función devuelve el mensaje de error
 * en lenguaje sencillo, o null si el valor es válido. Los ViewModels las usan y la UI solo
 * muestra el resultado.
 */
object Validador {

    const val MIN_CONTRASENA = 8
    const val MAX_SERVICIOS = 2
    const val MAX_TITULO = 40
    const val MIN_DESCRIPCION = 10
    const val MAX_DESCRIPCION = 300
    const val MAX_MENSAJE = 500

    private val REGEX_NOMBRE = Regex("^[\\p{L} .'-]+$")
    private val REGEX_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")

    /** Reglas de Instagram: 1-30 caracteres, letras, números, punto y guion bajo; sin puntos al inicio/fin ni dobles. */
    private val REGEX_INSTAGRAM = Regex("^(?!\\.)(?!.*\\.\\.)(?!.*\\.$)[A-Za-z0-9._]{1,30}$")

    // ---------- Cuenta ----------

    fun nombre(valor: String): String? {
        val v = valor.trim()
        return when {
            v.isEmpty() -> "Escribe tu nombre"
            v.length < 3 -> "Debe tener al menos 3 letras"
            !REGEX_NOMBRE.matches(v) -> "Usa solo letras y espacios"
            else -> null
        }
    }

    fun correo(valor: String): String? {
        val v = valor.trim()
        return when {
            v.isEmpty() -> "Escribe tu correo"
            !REGEX_CORREO.matches(v) -> "Correo no válido (ejemplo: nombre@correo.cl)"
            else -> null
        }
    }

    /** Contraseña nueva: mínimo 8 caracteres con al menos una letra y un número. */
    fun contrasenaNueva(valor: String): String? = when {
        valor.isEmpty() -> "Escribe una contraseña"
        valor.length < MIN_CONTRASENA -> "Mínimo $MIN_CONTRASENA caracteres"
        valor.none { it.isLetter() } -> "Debe tener al menos una letra"
        valor.none { it.isDigit() } -> "Debe tener al menos un número"
        else -> null
    }

    /** Al iniciar sesión solo se exige que no venga vacía; la comprobación real la hace el repositorio. */
    fun contrasenaIngreso(valor: String): String? =
        if (valor.isEmpty()) "Escribe tu contraseña" else null

    fun confirmacion(contrasena: String, confirmacion: String): String? = when {
        confirmacion.isEmpty() -> "Repite tu contraseña"
        confirmacion != contrasena -> "Las contraseñas no coinciden"
        else -> null
    }

    fun terminos(aceptados: Boolean): String? =
        if (!aceptados) "Debes aceptar para crear tu cuenta" else null

    // ---------- Perfil ----------

    fun region(valor: String): String? =
        if (valor !in RegionesChile.nombres) "Elige tu región" else null

    /**
     * La comuna debe pertenecer a la región elegida. Esto evita el problema del caso:
     * fichas "invisibles" por quedar con una comuna de otra región.
     */
    fun comuna(region: String, comuna: String): String? = when {
        comuna.isBlank() -> "Elige tu comuna"
        comuna !in RegionesChile.comunasDe(region) -> "Esa comuna no pertenece a la región elegida"
        else -> null
    }

    /** Si declara un emprendimiento, debe elegir entre 1 y 2 servicios (regla del directorio). */
    fun servicios(empresa: String, servicios: List<Servicio>): String? = when {
        servicios.size > MAX_SERVICIOS -> "Elige como máximo $MAX_SERVICIOS servicios"
        empresa.isNotBlank() && servicios.isEmpty() -> "Elige al menos 1 servicio de tu emprendimiento"
        else -> null
    }

    /** Opcional. Si se escribe, debe ser un celular chileno. */
    fun telefono(valor: String): String? =
        if (valor.isBlank() || normalizarTelefono(valor) != null) null
        else "Celular no válido (ejemplo: +56 9 1234 5678)"

    /** Opcional. Acepta "@usuario", "usuario" o el enlace completo; evita enlaces de Instagram rotos. */
    fun instagram(valor: String): String? =
        if (valor.isBlank() || normalizarInstagram(valor) != null) null
        else "Usuario de Instagram no válido (ejemplo: @mi_taller)"

    /** "912345678", "+56912345678" o "9 1234 5678" → "+56 9 1234 5678". Null si no es un celular válido. */
    fun normalizarTelefono(valor: String): String? {
        val digitos = valor.filter { it.isDigit() }
        val local = when {
            digitos.length == 11 && digitos.startsWith("56") -> digitos.substring(2)
            digitos.length == 9 -> digitos
            else -> return null
        }
        if (!local.startsWith("9")) return null
        return "+56 9 ${local.substring(1, 5)} ${local.substring(5)}"
    }

    /** "https://www.instagram.com/Mi.Taller/?igsh=x" o "@Mi.Taller" → "mi.taller". Null si no es válido. */
    fun normalizarInstagram(valor: String): String? {
        val usuario = valor.trim()
            .substringBefore('?')
            .removePrefix("https://").removePrefix("http://")
            .removePrefix("www.")
            .removePrefix("instagram.com/")
            .removePrefix("@")
            .trimEnd('/')
        return if (REGEX_INSTAGRAM.matches(usuario)) usuario.lowercase() else null
    }

    // ---------- Catálogo ----------

    fun fotoTrabajo(ruta: String?): String? =
        if (ruta == null) "Agrega una foto con la cámara o desde la galería" else null

    fun tituloTrabajo(valor: String): String? {
        val v = valor.trim()
        return when {
            v.isEmpty() -> "Escribe un título"
            v.length < 3 -> "Mínimo 3 caracteres"
            v.length > MAX_TITULO -> "Máximo $MAX_TITULO caracteres"
            else -> null
        }
    }

    fun descripcionTrabajo(valor: String): String? {
        val v = valor.trim()
        return when {
            v.isEmpty() -> "Cuenta brevemente qué hiciste"
            v.length < MIN_DESCRIPCION -> "Mínimo $MIN_DESCRIPCION caracteres"
            v.length > MAX_DESCRIPCION -> "Máximo $MAX_DESCRIPCION caracteres"
            else -> null
        }
    }

    fun servicioTrabajo(servicio: Servicio?): String? =
        if (servicio == null) "Elige una categoría" else null

    // ---------- Mensajes ----------

    fun mensaje(valor: String): String? = when {
        valor.isBlank() -> "Escribe un mensaje"
        valor.trim().length > MAX_MENSAJE -> "Máximo $MAX_MENSAJE caracteres"
        else -> null
    }
}
