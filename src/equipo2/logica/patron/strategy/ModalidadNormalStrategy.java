package equipo2.logica.patron.strategy;

/**
 * Implementación del <b>patrón Strategy</b> para la modalidad <b>NORMAL</b>.
 *
 * <p>En esta modalidad, el cliente <b>no recibe ningún descuento</b>:
 * el total a pagar es exactamente igual al importe base calculado por el envío.</p>
 *
 * <p>Esta es la estrategia por defecto cuando se inicia la aplicación.</p>
 *
 * <p>Arquitectura: capa <b>Lógica</b> — implementación concreta del patrón <b>Strategy</b>.
 * Implementa {@link ModalidadStrategy}.</p>
 */
public class ModalidadNormalStrategy implements ModalidadStrategy {

    // ============================================================
    // PATRÓN STRATEGY: implementación concreta "sin descuento"
    // ============================================================

    /**
     * Retorna el nombre de esta modalidad.
     *
     * @return "NORMAL"
     */
    @Override
    public String getNombre() {
        return "NORMAL";
    }

    /**
     * No aplica ningún descuento: retorna siempre 0.
     *
     * @param importeBase el importe bruto del envío (no se usa en este cálculo)
     * @return 0.0 (sin descuento en modalidad NORMAL)
     */
    @Override
    public double calcularDescuento(double importeBase) {
        // Modalidad NORMAL: sin descuento
        return 0.0;
    }

    /**
     * El total a pagar es igual al importe base, sin ninguna reducción.
     *
     * @param importeBase el importe bruto del envío
     * @return el mismo importe base, sin descuento
     */
    @Override
    public double calcularTotal(double importeBase) {
        // Sin descuento: el cliente paga el 100% del importe base
        return importeBase;
    }
}
