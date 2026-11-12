package equipo2.logica.patron.strategy;

/**
 * Patrón de diseño de Comportamiento: Strategy.
 * Define la estrategia para calcular descuentos y totales según la modalidad (NORMAL o BENEFICIO).
 */
public interface ModalidadStrategy {
    String getNombre();
    double calcularDescuento(double importeBase);
    double calcularTotal(double importeBase);
}
