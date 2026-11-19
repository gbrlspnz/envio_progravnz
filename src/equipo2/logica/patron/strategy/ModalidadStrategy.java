package equipo2.logica.patron.strategy;

/**
 * Interfaz que define el <b>patrón de diseño Strategy</b> para las modalidades de cotización.
 *
 * <p><b>¿Por qué usamos el patrón Strategy?</b><br>
 * El patrón Strategy permite encapsular algoritmos intercambiables detrás de una misma
 * interfaz. En este sistema, la forma de calcular el descuento cambia según la modalidad
 * del cliente (NORMAL o BENEFICIO), pero el resto del código no necesita saber cuál se usa.</p>
 *
 * <p>Las implementaciones concretas de esta interfaz son:</p>
 * <ul>
 *   <li>{@link ModalidadNormalStrategy}   – sin descuento (paga el 100%)</li>
 *   <li>{@link ModalidadBeneficioStrategy} – con 10% de descuento sobre el importe base</li>
 * </ul>
 *
 * <p>La vista ({@link equipo2.vista.EnvioApp}) selecciona la estrategia mediante
 * botones de radio y la aplica al calcular el total de cada envío seleccionado.</p>
 *
 * <p>Arquitectura: capa <b>Lógica</b> — patrón <b>Strategy</b>.</p>
 */
public interface ModalidadStrategy {

    /**
     * Retorna el nombre descriptivo de esta modalidad.
     * Se usa para mostrar en la interfaz gráfica qué modalidad está activa.
     *
     * @return nombre de la modalidad (ej: "NORMAL", "BENEFICIO")
     */
    String getNombre();

    /**
     * Calcula el descuento a aplicar sobre el importe base del envío.
     *
     * @param importeBase el importe bruto del envío antes de descuentos
     * @return el monto del descuento en pesos (puede ser 0.0 si no hay descuento)
     */
    double calcularDescuento(double importeBase);

    /**
     * Calcula el total final a pagar por el envío, aplicando el descuento correspondiente.
     *
     * @param importeBase el importe bruto del envío antes de descuentos
     * @return el total a pagar en pesos (importe base menos el descuento)
     */
    double calcularTotal(double importeBase);
}
