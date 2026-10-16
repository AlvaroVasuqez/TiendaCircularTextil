package cl.duoc.textilcircular.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tablas de la base de datos local (Room / SQLite).
 * Listas y enums se guardan como texto (nombres del enum separados por coma) para no
 * necesitar TypeConverters; la conversión se hace en los repositorios.
 */

@Entity(tableName = "cuentas", indices = [Index(value = ["correo"], unique = true)])
data class CuentaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val correo: String,
    /** Nunca se guarda la contraseña: solo su hash SHA-256 con una sal aleatoria. */
    val hashContrasena: String,
    val sal: String,
    val nombre: String,
    val empresa: String = "",
    val region: String = "",
    val comuna: String = "",
    val servicios: String = "",
    val antiguedad: String? = null,
    val telefono: String = "",
    val instagram: String = "",
    val sesionActiva: Boolean = false,
    val creadaEn: Long
)

@Entity(
    tableName = "trabajos",
    foreignKeys = [
        ForeignKey(
            entity = CuentaEntity::class,
            parentColumns = ["id"],
            childColumns = ["cuentaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("cuentaId")]
)
data class TrabajoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cuentaId: Long,
    val titulo: String,
    val descripcion: String,
    val servicio: String,
    val rutaFoto: String,
    val creadoEn: Long
)

@Entity(
    tableName = "mensajes",
    foreignKeys = [
        ForeignKey(
            entity = CuentaEntity::class,
            parentColumns = ["id"],
            childColumns = ["cuentaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("cuentaId"), Index("emprendimientoId")]
)
data class MensajeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cuentaId: Long,
    val emprendimientoId: Int,
    val texto: String,
    val esMio: Boolean,
    val enviadoEn: Long
)
