package cl.duoc.textilcircular.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a datos. Las consultas que devuelven Flow se vuelven a emitir solas cada vez
 * que cambia una tabla involucrada, así la UI siempre muestra lo que hay en la base.
 * Catálogo y mensajes se filtran por la cuenta con sesión activa (JOIN con cuentas).
 */
@Dao
abstract class CuentaDao {
    @Query("SELECT * FROM cuentas WHERE sesionActiva = 1 LIMIT 1")
    abstract fun observarSesion(): Flow<CuentaEntity?>

    @Query("SELECT id FROM cuentas WHERE sesionActiva = 1 LIMIT 1")
    abstract suspend fun idSesionActiva(): Long?

    @Query("SELECT * FROM cuentas WHERE correo = :correo LIMIT 1")
    abstract suspend fun buscarPorCorreo(correo: String): CuentaEntity?

    @Insert
    abstract suspend fun insertar(cuenta: CuentaEntity): Long

    @Query(
        """UPDATE cuentas SET nombre = :nombre, empresa = :empresa, region = :region, comuna = :comuna,
           servicios = :servicios, antiguedad = :antiguedad, telefono = :telefono, instagram = :instagram
           WHERE sesionActiva = 1"""
    )
    abstract suspend fun actualizarPerfil(
        nombre: String,
        empresa: String,
        region: String,
        comuna: String,
        servicios: String,
        antiguedad: String?,
        telefono: String,
        instagram: String
    )

    @Query("UPDATE cuentas SET sesionActiva = 0")
    abstract suspend fun cerrarSesiones()

    @Query("UPDATE cuentas SET sesionActiva = 1 WHERE id = :id")
    protected abstract suspend fun activar(id: Long)

    /** Solo una cuenta puede tener la sesión abierta: se cierran todas y se activa una. */
    @Transaction
    open suspend fun iniciarSesion(id: Long) {
        cerrarSesiones()
        activar(id)
    }
}

@Dao
abstract class TrabajoDao {
    @Query(
        """SELECT t.* FROM trabajos t INNER JOIN cuentas c ON c.id = t.cuentaId
           WHERE c.sesionActiva = 1 ORDER BY t.creadoEn DESC"""
    )
    abstract fun observarDeSesion(): Flow<List<TrabajoEntity>>

    @Query("SELECT * FROM trabajos WHERE id = :id")
    abstract suspend fun buscar(id: Long): TrabajoEntity?

    @Insert
    abstract suspend fun insertar(trabajo: TrabajoEntity): Long

    @Delete
    abstract suspend fun eliminar(trabajo: TrabajoEntity)
}

@Dao
abstract class MensajeDao {
    @Query(
        """SELECT m.* FROM mensajes m INNER JOIN cuentas c ON c.id = m.cuentaId
           WHERE c.sesionActiva = 1 ORDER BY m.enviadoEn, m.id"""
    )
    abstract fun observarDeSesion(): Flow<List<MensajeEntity>>

    @Insert
    abstract suspend fun insertar(mensaje: MensajeEntity): Long

    @Insert
    abstract suspend fun insertarTodos(mensajes: List<MensajeEntity>)
}
