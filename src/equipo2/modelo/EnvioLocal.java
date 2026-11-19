package equipo2.modelo;

/**
 * Representa un envío de tipo <b>local</b> (dentro de la misma ciudad o zona).
 *
 * <p>Extiende {@link Envio} e implementa el cálculo del importe con la fórmula
 * específica para envíos locales:</p>
 * <pre>
 *   importe = $2.000 (tarifa base fija) + (peso en kg × tarifa por kg)
 * </pre>
 *
 * <p>El atributo extra de este tipo es {@code tarifaPorKg}, que varía según
 * el acuerdo comercial con el cliente (lo ingresa el usuario al registrar el envío).</p>
 *
 * <p>Arquitectura: capa <b>Modelo</b> del patrón MVC.
 * Demuestra <b>herencia</b> al heredar de {@link Envio} y <b>polimorfismo</b>
 * al sobrescribir {@link #calcularImporte()}.</p>
 */
public class EnvioLocal extends Envio {

    // ============================================================
    // Atributo específico del envío local
    // ============================================================

    /**
     * Tarifa variable cobrada por kilogramo de peso.
     * El costo total dependerá de esta tarifa multiplicada por el peso del paquete.
     */
    private double tarifaPorKg;

    // ============================================================
    // Constructor
    // ============================================================

    /**
     * Construye un envío local con todos sus datos.
     *
     * @param codigo      código único identificador del envío
     * @param destino     destino del paquete (ciudad o barrio)
     * @param pesoKg      peso del paquete en kilogramos (entre 0.1 y 50)
     * @param tarifaPorKg tarifa en pesos cobrada por cada kilogramo (debe ser &gt; 0)
     */
    public EnvioLocal(String codigo, String destino, double pesoKg, double tarifaPorKg) {
        // Llamamos al constructor de la superclase con los atributos comunes
        super(codigo, destino, pesoKg);
        this.tarifaPorKg = tarifaPorKg;
    }

    // ============================================================
    // Getters y Setters
    // ============================================================

    /** @return la tarifa por kilogramo aplicada a este envío local */
    public double getTarifaPorKg() { return tarifaPorKg; }

    /** @param tarifaPorKg nueva tarifa por kilogramo */
    public void setTarifaPorKg(double tarifaPorKg) { this.tarifaPorKg = tarifaPorKg; }

    // ============================================================
    // Implementación del método abstracto (POLIMORFISMO)
    // ============================================================

    /**
     * Calcula el importe base de un envío local.
     *
     * <p>Fórmula: <b>$2.000 (fijo) + (peso × tarifaPorKg)</b></p>
     * <p>El valor fijo de $2.000 representa el costo mínimo del servicio local.</p>
     *
     * @return el importe total del envío en pesos argentinos
     */
    @Override
    public double calcularImporte() {
        // Tarifa base fija de $2.000 más el costo variable según peso y tarifa acordada
        return 2000.0 + (getPesoKg() * this.tarifaPorKg);
    }
}
