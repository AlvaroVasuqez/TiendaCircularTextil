package cl.duoc.textilcircular.data.model

data class Region(val nombre: String, val comunas: List<String>)

/**
 * Regiones y comunas como listas cerradas. Los nombres de región siguen el formato
 * que usa el Excel del directorio ("Región de Valparaíso", "Región Metropolitana").
 * Elegir de una lista (en vez de escribir) evita comunas mal tipeadas.
 */
object RegionesChile {
    val regiones: List<Region> = listOf(
        Region("Región de Arica y Parinacota", listOf("Arica", "Camarones", "Putre", "General Lagos")),
        Region("Región de Tarapacá", listOf("Iquique", "Alto Hospicio", "Pozo Almonte", "Pica")),
        Region("Región de Antofagasta", listOf("Antofagasta", "Calama", "Tocopilla", "Mejillones", "Taltal")),
        Region("Región de Atacama", listOf("Copiapó", "Vallenar", "Caldera", "Chañaral")),
        Region("Región de Coquimbo", listOf("La Serena", "Coquimbo", "Ovalle", "Illapel", "Vicuña")),
        Region(
            "Región de Valparaíso",
            listOf("Valparaíso", "Viña del Mar", "Quilpué", "Villa Alemana", "Quillota", "San Antonio", "Los Andes", "Concón")
        ),
        Region(
            "Región Metropolitana",
            listOf("Santiago", "Providencia", "Maipú", "Puente Alto", "La Florida", "Ñuñoa", "Las Condes", "Recoleta")
        ),
        Region("Región de O'Higgins", listOf("Rancagua", "San Fernando", "Rengo", "Santa Cruz", "Pichilemu")),
        Region("Región del Maule", listOf("Talca", "Curicó", "Linares", "Constitución", "Cauquenes")),
        Region("Región del Ñuble", listOf("Chillán", "San Carlos", "Bulnes", "Quirihue")),
        Region("Región del Biobío", listOf("Concepción", "Talcahuano", "Los Ángeles", "Coronel", "Chiguayante")),
        Region("Región de La Araucanía", listOf("Temuco", "Padre Las Casas", "Villarrica", "Angol", "Pucón")),
        Region("Región de Los Ríos", listOf("Valdivia", "La Unión", "Río Bueno", "Panguipulli")),
        Region("Región de Los Lagos", listOf("Puerto Montt", "Osorno", "Castro", "Ancud", "Puerto Varas")),
        Region("Región de Aysén", listOf("Coyhaique", "Puerto Aysén", "Chile Chico", "Cochrane")),
        Region("Región de Magallanes", listOf("Punta Arenas", "Puerto Natales", "Porvenir", "Cabo de Hornos"))
    )

    val nombres: List<String> = regiones.map { it.nombre }

    fun comunasDe(region: String): List<String> =
        regiones.firstOrNull { it.nombre == region }?.comunas ?: emptyList()
}
