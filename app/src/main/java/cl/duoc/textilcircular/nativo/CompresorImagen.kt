package cl.duoc.textilcircular.nativo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

/**
 * Recurso nativo: fotos de la cámara y la galería.
 * Antes de guardar, la imagen se optimiza (requisito del caso: ahorro de datos y equipos de
 * gama baja): se corrige la rotación, se reduce a 1280 px por lado y se guarda en JPEG al 80 %.
 * Las fotos quedan en el almacenamiento interno de la app (privado, no visible para otras apps).
 */
class CompresorImagen(private val contexto: Context) {

    data class Resultado(val ruta: String, val bytesOriginales: Long, val bytesFinales: Long)

    /** Archivo temporal donde la app de cámara escribe la foto (compartido de forma segura con FileProvider). */
    fun crearUriParaCamara(): Uri {
        val carpeta = File(contexto.cacheDir, CARPETA_CAMARA).apply { mkdirs() }
        val archivo = File(carpeta, "foto_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(contexto, "${contexto.packageName}.fileprovider", archivo)
    }

    /** Operación bloqueante: llamar desde Dispatchers.IO. Lanza IOException si la imagen no se puede leer. */
    fun comprimir(uri: Uri): Resultado {
        val bytes = contexto.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IOException("No se pudo abrir la imagen")

        val dimensiones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, dimensiones)
        if (dimensiones.outWidth <= 0 || dimensiones.outHeight <= 0) throw IOException("El archivo no es una imagen")

        val opciones = BitmapFactory.Options().apply {
            inSampleSize = calcularMuestreo(dimensiones.outWidth, dimensiones.outHeight, LADO_MAXIMO)
        }
        var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opciones)
            ?: throw IOException("No se pudo decodificar la imagen")
        bitmap = reescalar(bitmap, LADO_MAXIMO)
        bitmap = rotarSegunExif(bitmap, bytes)

        val carpeta = File(contexto.filesDir, CARPETA_CATALOGO).apply { mkdirs() }
        val destino = File(carpeta, "${UUID.randomUUID()}.jpg")
        FileOutputStream(destino).use { salida ->
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, salida)) {
                throw IOException("No se pudo guardar la imagen")
            }
        }
        bitmap.recycle()
        return Resultado(destino.absolutePath, bytes.size.toLong(), destino.length())
    }

    fun eliminar(ruta: String?) {
        if (ruta != null) File(ruta).delete()
    }

    fun limpiarTemporalesCamara() {
        File(contexto.cacheDir, CARPETA_CAMARA).listFiles()?.forEach { it.delete() }
    }

    private fun reescalar(original: Bitmap, ladoMaximo: Int): Bitmap {
        val mayor = maxOf(original.width, original.height)
        if (mayor <= ladoMaximo) return original
        val factor = ladoMaximo.toFloat() / mayor
        val escalado = Bitmap.createScaledBitmap(
            original, (original.width * factor).toInt(), (original.height * factor).toInt(), true
        )
        if (escalado != original) original.recycle()
        return escalado
    }

    /** Muchas cámaras guardan la foto "acostada" y anotan el giro en EXIF; aquí se aplica ese giro. */
    private fun rotarSegunExif(original: Bitmap, bytes: ByteArray): Bitmap {
        val orientacion = try {
            ExifInterface(ByteArrayInputStream(bytes))
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } catch (_: IOException) {
            ExifInterface.ORIENTATION_NORMAL
        }
        val grados = when (orientacion) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> return original
        }
        val matriz = Matrix().apply { postRotate(grados) }
        val rotado = Bitmap.createBitmap(original, 0, 0, original.width, original.height, matriz, true)
        if (rotado != original) original.recycle()
        return rotado
    }

    companion object {
        private const val LADO_MAXIMO = 1280
        private const val CALIDAD_JPEG = 80
        private const val CARPETA_CAMARA = "camara"
        private const val CARPETA_CATALOGO = "catalogo"

        /** Lee una foto guardada en tamaño reducido para mostrarla en pantalla sin gastar memoria. */
        fun decodificar(ruta: String, ladoMaximo: Int): Bitmap? {
            val dimensiones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(ruta, dimensiones)
            if (dimensiones.outWidth <= 0) return null
            val opciones = BitmapFactory.Options().apply {
                inSampleSize = calcularMuestreo(dimensiones.outWidth, dimensiones.outHeight, ladoMaximo)
            }
            return BitmapFactory.decodeFile(ruta, opciones)
        }

        /** Mayor potencia de 2 que deja la imagen aún por sobre el tamaño pedido. */
        private fun calcularMuestreo(ancho: Int, alto: Int, ladoMaximo: Int): Int {
            var muestreo = 1
            while (maxOf(ancho, alto) / (muestreo * 2) >= ladoMaximo) muestreo *= 2
            return muestreo
        }
    }
}
