package cl.duoc.textilcircular.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** Base de datos SQLite de la app. Todo queda guardado en el teléfono y funciona sin internet. */
@Database(
    entities = [CuentaEntity::class, TrabajoEntity::class, MensajeEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TextilDatabase : RoomDatabase() {
    abstract fun cuentaDao(): CuentaDao
    abstract fun trabajoDao(): TrabajoDao
    abstract fun mensajeDao(): MensajeDao

    companion object {
        private const val NOMBRE_ARCHIVO = "textil_circular.db"

        fun crear(contexto: Context): TextilDatabase =
            Room.databaseBuilder(contexto, TextilDatabase::class.java, NOMBRE_ARCHIVO).build()
    }
}
