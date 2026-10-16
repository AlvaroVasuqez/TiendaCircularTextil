package cl.duoc.textilcircular.data.repository

import android.database.sqlite.SQLiteConstraintException
import cl.duoc.textilcircular.data.local.CuentaDao
import cl.duoc.textilcircular.data.local.CuentaEntity
import cl.duoc.textilcircular.data.local.MensajeDao
import cl.duoc.textilcircular.data.local.MensajeEntity
import cl.duoc.textilcircular.data.model.Antiguedad
import cl.duoc.textilcircular.data.model.PerfilUsuario
import cl.duoc.textilcircular.data.model.Servicio
import cl.duoc.textilcircular.data.seguridad.Contrasenas
import cl.duoc.textilcircular.nativo.Notificador
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CuentaRepositoryRoom(
    private val cuentaDao: CuentaDao,
    private val mensajeDao: MensajeDao,
    private val notificador: Notificador
) : CuentaRepository {

    override val sesion: Flow<PerfilUsuario?> = cuentaDao.observarSesion().map { it?.aPerfil() }

    override suspend fun registrar(nombre: String, correo: String, contrasena: String): Boolean {
        val correoNormalizado = correo.trim().lowercase()
        if (cuentaDao.buscarPorCorreo(correoNormalizado) != null) return false
        val sal = Contrasenas.generarSal()
        val id = try {
            cuentaDao.insertar(
                CuentaEntity(
                    correo = correoNormalizado,
                    hashContrasena = Contrasenas.hash(contrasena, sal),
                    sal = sal,
                    nombre = nombre.trim(),
                    creadaEn = System.currentTimeMillis()
                )
            )
        } catch (_: SQLiteConstraintException) {
            return false
        }
        cuentaDao.iniciarSesion(id)
        // Reemplaza el correo de bienvenida manual que hoy envía la Fundación.
        notificador.bienvenida(nombre.trim().substringBefore(' '))
        return true
    }

    override suspend fun iniciarSesion(correo: String, contrasena: String): Boolean {
        val cuenta = cuentaDao.buscarPorCorreo(correo.trim().lowercase()) ?: return false
        if (!Contrasenas.coincide(contrasena, cuenta.sal, cuenta.hashContrasena)) return false
        cuentaDao.iniciarSesion(cuenta.id)
        return true
    }

    override suspend fun entrarConDemo() {
        val existente = cuentaDao.buscarPorCorreo(DatosDeEjemplo.CORREO_DEMO)
        if (existente != null) {
            cuentaDao.iniciarSesion(existente.id)
            return
        }
        val sal = Contrasenas.generarSal()
        val id = cuentaDao.insertar(
            CuentaEntity(
                correo = DatosDeEjemplo.CORREO_DEMO,
                hashContrasena = Contrasenas.hash(DatosDeEjemplo.CONTRASENA_DEMO, sal),
                sal = sal,
                nombre = "Andrea Soto",
                empresa = "Puntadas del Puerto",
                region = "Región de Valparaíso",
                comuna = "Valparaíso",
                servicios = Servicio.REPARACION.name,
                antiguedad = Antiguedad.ENTRE_1_3.name,
                creadaEn = System.currentTimeMillis()
            )
        )
        val ahora = System.currentTimeMillis()
        mensajeDao.insertarTodos(
            DatosDeEjemplo.mensajesDemo.map {
                MensajeEntity(
                    cuentaId = id,
                    emprendimientoId = it.emprendimientoId,
                    texto = it.texto,
                    esMio = it.esMio,
                    enviadoEn = ahora - it.minutosAtras * 60_000
                )
            }
        )
        cuentaDao.iniciarSesion(id)
    }

    override suspend fun cerrarSesion() = cuentaDao.cerrarSesiones()

    override suspend fun guardarPerfil(perfil: PerfilUsuario) {
        cuentaDao.actualizarPerfil(
            nombre = perfil.nombre,
            empresa = perfil.empresa,
            region = perfil.region,
            comuna = perfil.comuna,
            servicios = perfil.servicios.joinToString(",") { it.name },
            antiguedad = perfil.antiguedad?.name,
            telefono = perfil.telefono,
            instagram = perfil.instagram
        )
    }

    private fun CuentaEntity.aPerfil() = PerfilUsuario(
        correo = correo,
        nombre = nombre,
        empresa = empresa,
        region = region,
        comuna = comuna,
        servicios = servicios.split(",").mapNotNull { nombre -> Servicio.entries.firstOrNull { it.name == nombre } },
        antiguedad = Antiguedad.entries.firstOrNull { it.name == antiguedad },
        telefono = telefono,
        instagram = instagram
    )
}
