package equipo2.modelo;

/**
 * Representa un envío de tipo <b>interurbano</b> (entre distintas ciudades o provincias).
 *
 * <p>Extiende {@link Envio} e implementa el cálculo del importe con la fórmula
 * específica para envíos interurbanos:</p>
 * <pre>
 *   importe = $5.000 (base fija) + (peso en kg × $1.500) + (distancia en km × $20)
 * </pre>
 *
 * <p>El atributo extra de este tipo es {@code distanciaKm}, que representa la
 * distancia entre el origen y el destino del envío.</p>
 *
 * <p>Arquitectura: capa <b>Modelo</b> del patrón MVC.
 * Demuestra <b>herencia</b> al heredar de {@link Envio} y <b>polimorfismo</b>
 * al sobrescribir {@link #calcularImporte()}.</p>
 */
public class EnvioInterurbano extends Envio {

    // ============================================================
    // Atributo específico del envío interurbano
    // ============================================================

    /**
     * Distancia en kilómetros entre el punto de origen y el destino.
     * Debe estar entre 1 y 2.000 km (según las reglas de negocio).
     */
    private double distanciaKm;

    // ============================================================
    // Constructor
    // ============================================================

    /**
     * Construye un envío interurbano con todos sus datos.
     *
     * @param codigo      código único identificador del envío
     * @param destino     ciudad de destino del paquete
     * @param pesoKg      peso del paquete en kilogramos (entre 0.1 y 50)
     * @param distanciaKm distancia al destino en kilómetros (entre 1 y 2.000)
     */
    public EnvioInterurbano(String codigo, String destino, double pesoKg, double distanciaKm) {
        // Llamamos al constructor de la superclase con los atributos comunes
        super(codigo, destino, pesoKg);
        this.distanciaKm = distanciaKm;
    }

    // ============================================================
    // Getters y Setters
    // ============================================================

    /** @return la distancia en kilómetros al destino */
    public double getDistanciaKm() { return distanciaKm; }

    /** @param distanciaKm nueva distancia en kilómetros */
    public void setDistanciaKm(double distanciaKm) { this.distanciaKm = distanciaKm; }

    // ============================================================
    // Implementación del método abstracto (POLIMORFISMO)
    // ============================================================

    /**
     * Calcula el importe base de un envío interurbano.
     *
     * <p>Fórmula: <b>$5.000 (fijo) + (peso × $1.500) + (distancia × $20)</b></p>
     * <ul>
     *   <li>$5.000: costo base del servicio interurbano</li>
     *   <li>$1.500 por kg: costo variable según el peso del paquete</li>
     *   <li>$20 por km: costo variable según la distancia al destino</li>
     * </ul>
     *
     * @return el importe total del envío en pesos argentinos
     */
    @Override
    public double calcularImporte() {
        // Tarifa fija + costo por peso ($1500/kg) + costo por distancia ($20/km)
        return 5000.0 + (getPesoKg() * 1500.0) + (this.distanciaKm * 20.0);
    }
}
