package equipo2.logica.patron.strategy;

/**
 * Implementación del <b>patrón Strategy</b> para la modalidad <b>BENEFICIO</b>.
 *
 * <p>En esta modalidad, el cliente recibe un <b>descuento del 10%</b> sobre
 * el importe base del envío. Es el caso especial para clientes con beneficios.</p>
 *
 * <p>Fórmula aplicada:</p>
 * <pre>
 *   descuento = importeBase × 0.10
 *   total     = importeBase - descuento  (equivale a importeBase × 0.90)
 * </pre>
 *
 * <p>Arquitectura: capa <b>Lógica</b> — implementación concreta del patrón <b>Strategy</b>.
 * Implementa {@link ModalidadStrategy}.</p>
 */
public class ModalidadBeneficioStrategy implements ModalidadStrategy {

    // ============================================================
    // PATRÓN STRATEGY: implementación concreta "con 10% de descuento"
    // ============================================================

    /**
     * Retorna el nombre de esta modalidad.
     *
     * @return "BENEFICIO"
     */
    @Override
    public String getNombre() {
        return "BENEFICIO";
    }

    /**
     * Calcula el 10% de descuento sobre el importe base del envío.
     *
     * @param importeBase el importe bruto del envío antes del descuento
     * @return el monto del descuento (10% del importe base)
     */
    @Override
    public double calcularDescuento(double importeBase) {
        // El descuento es el 10% del importe bruto
        return importeBase * 0.10;
    }

    /**
     * Calcula el total a pagar aplicando el descuento del 10%.
     *
     * @param importeBase el importe bruto del envío antes del descuento
     * @return el total a pagar (90% del importe base)
     */
    @Override
    public double calcularTotal(double importeBase) {
        // El cliente paga el importe menos el descuento calculado
        return importeBase - calcularDescuento(importeBase);
    }
}
