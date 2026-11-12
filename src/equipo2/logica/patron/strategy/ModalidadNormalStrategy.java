package equipo2.logica.patron.strategy;

/**
 * Estrategia de cálculo para Modalidad NORMAL:
 * No aplica descuento (0%), el total es igual al importe base.
 */
public class ModalidadNormalStrategy implements ModalidadStrategy {

    @Override
    public String getNombre() {
        return "NORMAL";
    }

    @Override
    public double calcularDescuento(double importeBase) {
        return 0.0;
    }

    @Override
    public double calcularTotal(double importeBase) {
        return importeBase;
    }
}
